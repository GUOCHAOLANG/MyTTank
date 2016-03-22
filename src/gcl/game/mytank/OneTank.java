package gcl.game.mytank;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

//坦克类，每个坦克都属于这种类型
public class OneTank {
	GameView gv;
	int enemyOrFriend;			//标识这个坦克是敌人还是自己：1、2代表敌人，0代表自己
	int tankLine,tankRow;		//标识这个坦克目前的位置：第tankLine行(0~38)，第tankRow列(0~30)
	int tankX,tankY;			//标识这个坦克的左上角的坐标：每个坦克20*20像素，
	//由上面的tankLine和tankRow可以很容易地算出：tankX=tankRow*10，tankY=tankLine*10
	int tankBirthLocation;		//标识坦克出生时的位置：敌人有3个可能1代表左上角，2代表中间，3代表右上角
								//我方坦克有两种出生时的位置：4代表下方左侧，5代表下方右侧
	int tankDir;				//标识坦克目前的运动方向：1代表向上；2代表向下；3代表向左；4代表向右
	int tankV;					//标识坦克的速度，每次向前移动多个块（每块10像素），如tankV=2，表示每次向前移动20像素
	int tankBlood;				//标识坦克的血量，每个坦克初始的血量不同，每中一子弹，则减去相应的血量，当血低于0时，该坦克死亡
	Bitmap bmpTank;				//标识当前坦克的图片
	Bitmap bmpEnemyTank1Up,bmpEnemyTank1Down,bmpEnemyTank1Left,bmpEnemyTank1Right;
	Bitmap bmpEnemyTank2Up,bmpEnemyTank2Down,bmpEnemyTank2Left,bmpEnemyTank2Right;
	Bitmap bmpMyTankUp,bmpMyTankDown,bmpMyTankLeft,bmpMyTankRight;
	Resources r;
	boolean myFlag,enemyFlag,enemyFireFlag;	//分别表示我方坦克运动、敌人坦克运动、敌人坦克发子弹标志
	int myTankSleepSpan,enemyTankSleepSpan;	//我方坦克、敌人坦克的睡眠时间，由这个时间可以决定坦克的运动速度
	int enemyFireSleepSpan;					//敌人隔多长时间发一个子弹
	int nBullets;				//记录我方坦克共发送了多少棵子弹
	int fireBulletType;			//我方坦克发出的子弹的类型：1或2
	public OneTank(final GameView gv,int eOf,int tl,int tr,int tbl,int td,int tv,int tb,int sleepSpan){
		this.gv=gv;
		enemyOrFriend=eOf;		//敌人或自己坦克
		tankLine=tl;			//坦克所在的行号
		tankRow=tr;				//坦克所在的列号
		tankX=tankRow*20;		//坦克的坐标
		tankY=tankLine*20;
		tankBirthLocation=tbl;	//坦克出生地
		tankDir=td;				//坦克的方向
		tankV=tv;				//坦克的速度
		tankBlood=tb;			//坦克的血量
		nBullets=0;
		fireBulletType=1;		//初始我方坦克发出的子弹类型为1号
		myFlag=enemyFlag=enemyFireFlag=true;	//标识我方坦克和敌人坦克的线程循环
		initBitmap();			//导入坦克图片
		if(eOf==0)
			myTankSleepSpan=sleepSpan;
		else
			enemyTankSleepSpan=sleepSpan;
		enemyFireSleepSpan=4000;		//敌人每2000毫秒发一个子弹
		if(enemyOrFriend==0){	//如果是我方坦克，则这样运动
			new Thread(){
				@Override
				public void run() {
					while(myFlag){
						if(gv.hasABonus==true && gv.oneBonus!=null){			//如果当前有Bonus的话
							if(tankLine==gv.oneBonus.bonusLine && tankRow==gv.oneBonus.bonusRow	){	//如果坦克经过Bonus，则捡到了一个Bonus
								switch(gv.oneBonus.type){		//根据Bonus的类型进行处理
								case 1:					//1号Bonus：炸弹，将敌人所有的坦克全部爆炸
									synchronized(gv.enemyTanks){
										int n;
										for(n=0;n<gv.enemyTanks.size();n++){
											OneTank one=gv.enemyTanks.get(n);
											one.enemyFireFlag=false;
											one.enemyFlag=false;
											gv.ta.myHandler.sendEmptyMessage(101);		//发送101号消息
										}
									}
									break;
								case 2:
									break;
								case 3:
									tankBlood=1000;		//3号Bonus：油箱，我方坦克血量加满成1000
									break;
								case 4:
									break;
								case 5:
									break;
								case 6:					//6号Bonus：铲子，我方城堡周围变成金刚石
									int i,j;
									for(i=36;i<=37;i++){
										for(j=13;j<=18;j++){
											gv.maps[i][j]=4;
										}
									}
									gv.maps[38][13]=gv.maps[38][14]=gv.maps[38][17]=gv.maps[38][18]=4;
									gv.maps[39][13]=gv.maps[39][14]=gv.maps[39][17]=gv.maps[39][18]=4;
									break;
								case 7:					//7号Bonus：我方坦克发出的子弹类型变为2
									fireBulletType=2;
									break;
								case 8:
									gv.nMyTanks++;		//8号Bonus：我方坦克数加一
									break;
								}
								gv.ta.myHandler.sendEmptyMessage(400);	//收回这个Bonus
							}
						}
						
						switch(gv.ta.keyCode){
						case 19:				//向上走
							if(tankDir!=1){		//如果当前方向不是向上方向，则只简单地更改坦克的朝向向上，而不移动
								tankDir=1;
							}else{
								if(canGoUp())	//如果能向上走，则向上走一步
									tankLine--;
							}
							break;
						case 20:				//向下走
							if(tankDir!=2){
								tankDir=2;
							}else{
								if(canGoDown())	//如果能向下走，则向下走一步
									tankLine++;
							}
							break;
						case 21:				//向左走
							if(tankDir!=3){
								tankDir=3;
							}else{
								if(canGoLeft())
									tankRow--;
							}
							break;
						case 22:				//向右走
							if(tankDir!=4){
								tankDir=4;
							}else{
								if(canGoRight())
									tankRow++;
							}
							break;
						case 62:				//空白键，发一个子弹
							Bullet oneBullet=new Bullet(gv,0,fireBulletType,tankLine,tankRow,tankDir,100);
							synchronized(gv.myBullets){		//线程同步，防止多个线程同时去操作这个链表
								gv.myBullets.add(oneBullet);		//将这个子弹添加到链表中
							}
							
							break;
						}
						gv.ta.keyCode=0;
						try {
							Thread.sleep(myTankSleepSpan);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}else{					//敌人坦克，则这样运动
			new Thread(){		//敌人坦克运动线程
				public void run() {
					while(enemyFlag){
						int zw=(int)(Math.random()*100);	//zw可得到0~99之间的随机整数
//						Log.d("概率",""+zw);
						if(zw<85){					//85%的概率按原来的方向走
							switch(tankDir){		//根据坦克当前的运动方向，决定它怎么向前运动
							case 1:					//向上
								if(canGoUp())
									tankLine--; 
								break;
							case 2:					//向下
								if(canGoDown())
									tankLine++;
								break;
							case 3:					//向左
								if(canGoLeft())
									tankRow--;
								break;
							case 4:					//向右
								if(canGoRight())
									tankRow++;
								break;
							}
						}else{						//20%的概率改变方向
							int newDir;				//新方向
							do{
								newDir=(int)(Math.random()*4)+1;		//概率得到1、2、3、4这4种方向
							}while(newDir==tankDir);	//直到得到一种方向与原来的方向不同的方向
							tankDir=newDir;				//改变坦克方向为新方向
						}
						try{
							Thread.sleep(enemyTankSleepSpan);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				};
			}.start();
			new Thread(){				//敌人自动发子弹线程，每1000毫秒发一个子弹
				public void run() {
					while(enemyFireFlag){
						Bullet oneBullet=new Bullet(gv,1,enemyOrFriend,
								tankLine,tankRow,tankDir,100);		//new出一个敌人子弹，型号与该坦克一致
						synchronized(gv.enemyBullets){		//线程同步，防止多个线程同时去操作这个链表
							gv.enemyBullets.add(oneBullet);		//将这个子弹添加到链表中
						}
						
						try{
							Thread.sleep(enemyFireSleepSpan);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
	}
	public boolean canGoUp(){		//判断目前能否向上走
		boolean canGo=true;
		if(tankLine==0)				//如果已经到了最上一行，不能再向上走
			canGo=false;
		else{
			int wt1=gv.maps[tankLine-1][tankRow];	//上一行的左小块物体
			int wt2=gv.maps[tankLine-1][tankRow+1];	//上一行的右小块物体
			if(wt1==2 || wt1==3 || wt1==4 || wt1==5 || wt2==2 || wt2==3 || wt2==4 || wt1==5){
				canGo=false;
			}
			//下面判断坦克之间是否发生冲突，以避免坦克之间互相穿越
			if(enemyOrFriend==0){					//如果当前的坦克是我方坦克，则只需要与所有的敌人坦克进行比较
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankLine==tankLine-2 && (enemyTankRow==tankRow-1 || enemyTankRow==tankRow || enemyTankRow==tankRow+1)){
							canGo=false;
							break;
						}
					}
				}
			}else{		//如果当前坦克为敌人坦克，一方面不能穿越我方坦克，另一方面也不能穿越别的坦克
				int otherLine,otherRow;			//别的坦克的行、列号
				otherLine=gv.myTank.tankLine;
				otherRow=gv.myTank.tankRow;
				//先检测与我方坦克冲突的情况
				if(otherLine==tankLine-2 && (otherRow==tankRow-1 || otherRow==tankRow || otherRow==tankRow+1))
					canGo=false;
				//再检测敌人坦克之间不得互相穿越
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankLine==tankLine-2 && (enemyTankRow==tankRow-1 || enemyTankRow==tankRow || enemyTankRow==tankRow+1)){
							canGo=false;
							break;
						}
					}
				}
			}
		}
		return canGo;
	}
	public boolean canGoDown(){		//判断目前能否向下走
		boolean canGo=true;
		if(tankLine==38)			//注意，虽然有0~39行，但是坦克的位置是0~38行，第39行是坦克的下半身
			canGo=false;
		else{
			int wt1=gv.maps[tankLine+2][tankRow];	//下一行的左小块物体，注意+1只是坦克自己的下半身，+2才是下一行
			int wt2=gv.maps[tankLine+2][tankRow+1];	//下一行的右小块物体
			if(wt1==2 || wt1==3 || wt1==4 || wt1==5 || wt2==2 || wt2==3 || wt2==4 || wt1==5){
				canGo=false;
			}
			//下面判断坦克之间是否发生冲突，以避免坦克之间互相穿越
			if(enemyOrFriend==0){					//如果当前的坦克是我方坦克，则只需要与所有的敌人坦克进行比较
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankLine==tankLine+2 && (enemyTankRow==tankRow-1 || enemyTankRow==tankRow || enemyTankRow==tankRow+1)){
							canGo=false;
							break;
						}
					}
				}
			}else{		//如果当前坦克为敌人坦克，一方面不能穿越我方坦克，另一方面也不能穿越别的坦克
				int otherLine,otherRow;			//别的坦克的行、列号
				otherLine=gv.myTank.tankLine;
				otherRow=gv.myTank.tankRow;
				//先检测与我方坦克冲突的情况
				if(otherLine==tankLine+2 && (otherRow==tankRow-1 || otherRow==tankRow || otherRow==tankRow+1))
					canGo=false;
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankLine==tankLine+2 && (enemyTankRow==tankRow-1 || enemyTankRow==tankRow || enemyTankRow==tankRow+1)){
							canGo=false;
							break;
						}
					}
				}
			}
		}
		return canGo;
	}
	public boolean canGoLeft(){		//判断目前能否向左走
		boolean canGo=true;
		if(tankRow==0)				//如果已经到了最左侧，不能再向左走了
			canGo=false;
		else{
			int wt1=gv.maps[tankLine][tankRow-1];	//本行左侧小块物体
			int wt2=gv.maps[tankLine+1][tankRow-1];	//下行左侧小块物体
			if(wt1==2 || wt1==3 || wt1==4 || wt1==5 || wt2==2 || wt2==3 || wt2==4 || wt1==5){
				canGo=false;
			}
			//下面判断坦克之间是否发生冲突，以避免坦克之间互相穿越
			if(enemyOrFriend==0){					//如果当前的坦克是我方坦克，则只需要与所有的敌人坦克进行比较
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankRow==tankRow-2 && (enemyTankLine==tankLine-1 || enemyTankLine==tankLine || enemyTankLine==tankLine+1)){
							canGo=false;
							break;
						}
					}
				}
			}else{		//如果当前坦克为敌人坦克，一方面不能穿越我方坦克，另一方面也不能穿越别的坦克
				int otherLine,otherRow;			//别的坦克的行、列号
				otherLine=gv.myTank.tankLine;
				otherRow=gv.myTank.tankRow;
				//先检测与我方坦克冲突的情况
				if(otherRow==tankRow-2 && (otherLine==tankLine-1 || otherLine==tankLine || otherLine==tankLine+1))
					canGo=false;
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankRow==tankRow-2 && (enemyTankLine==tankLine-1 || enemyTankLine==tankLine || enemyTankLine==tankLine+1)){
							canGo=false;
							break;
						}
					}
				}
			}
		}
		return canGo;
	}
	public boolean canGoRight(){	//判断目前能否向右走
		boolean canGo=true;
		if(tankRow==30)		//如果已经到了最右侧，注意共有0~31列，但是第31列是坦克的右半身，所以坦克只能到0~30列
			canGo=false;
		else{
			int wt1=gv.maps[tankLine][tankRow+2];	//本行右侧小块物体，注意+1只是坦克自己的右半身，+2才是右侧
			int wt2=gv.maps[tankLine+1][tankRow+2];	//下行右侧小块物体
			if(wt1==2 || wt1==3 || wt1==4 || wt1==5 || wt2==2 || wt2==3 || wt2==4 || wt1==5){
				canGo=false;
			}
			//下面判断坦克之间是否发生冲突，以避免坦克之间互相穿越
			if(enemyOrFriend==0){					//如果当前的坦克是我方坦克，则只需要与所有的敌人坦克进行比较
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankRow==tankRow+2 && (enemyTankLine==tankLine-1 || enemyTankLine==tankLine || enemyTankLine==tankLine+1)){
							canGo=false;
							break;
						}
					}
				}
			}else{		//如果当前坦克为敌人坦克，一方面不能穿越我方坦克，另一方面也不能穿越别的坦克
				int otherLine,otherRow;			//别的坦克的行、列号
				otherLine=gv.myTank.tankLine;
				otherRow=gv.myTank.tankRow;
				//先检测与我方坦克冲突的情况
				if(otherRow==tankRow+2 && (otherLine==tankLine-1 || otherLine==tankLine || otherLine==tankLine+1))
					canGo=false;
				OneTank one;
				int i;
				for(i=0;i<gv.enemyTanks.size();i++){
					one=gv.enemyTanks.get(i);		//取到一个敌人坦克
					if(one!=null){					//如果这个敌人坦克还存在，防止敌人坦克被你消灭了
						int enemyTankLine,enemyTankRow;		//读取敌人坦克的行和列号
						enemyTankLine=one.tankLine;
						enemyTankRow=one.tankRow;
						if(enemyTankRow==tankRow+2 && (enemyTankLine==tankLine-1 || enemyTankLine==tankLine || enemyTankLine==tankLine+1)){
							canGo=false;
							break;
						}
					}
				}
			}
		}
		return canGo;
	}
	public void initBitmap(){
		r=gv.getResources();
		bmpEnemyTank1Up=BitmapFactory.decodeResource(r, R.drawable.enemytank1up);
		bmpEnemyTank1Down=BitmapFactory.decodeResource(r, R.drawable.enemytank1down);
		bmpEnemyTank1Left=BitmapFactory.decodeResource(r, R.drawable.enemytank1left);
		bmpEnemyTank1Right=BitmapFactory.decodeResource(r, R.drawable.enemytank1right);
		bmpEnemyTank2Up=BitmapFactory.decodeResource(r, R.drawable.enemytank2up);
		bmpEnemyTank2Down=BitmapFactory.decodeResource(r, R.drawable.enemytank2down);
		bmpEnemyTank2Left=BitmapFactory.decodeResource(r, R.drawable.enemytank2left);
		bmpEnemyTank2Right=BitmapFactory.decodeResource(r, R.drawable.enemytank2right);
		bmpMyTankUp=BitmapFactory.decodeResource(r, R.drawable.mytankup);
		bmpMyTankDown=BitmapFactory.decodeResource(r, R.drawable.mytankdown);
		bmpMyTankLeft=BitmapFactory.decodeResource(r, R.drawable.mytanklleft);
		bmpMyTankRight=BitmapFactory.decodeResource(r, R.drawable.mytankright);
	}
	public void getBitmap(){					//根据坦克的属性获取坦克的当前图片
		if(enemyOrFriend==0){					//我方坦克
			switch(tankDir){
			case 1:
				bmpTank=bmpMyTankUp;
				break;
			case 2:
				bmpTank=bmpMyTankDown;
				break;
			case 3:
				bmpTank=bmpMyTankLeft;
				break;
			case 4:
				bmpTank=bmpMyTankRight;
				break;
			}
		}else if(enemyOrFriend==1){				//敌人坦克1
			switch(tankDir){
			case 1:
				bmpTank=bmpEnemyTank1Up;
				break;
			case 2:
				bmpTank=bmpEnemyTank1Down;
				break;
			case 3:
				bmpTank=bmpEnemyTank1Left;
				break;
			case 4:
				bmpTank=bmpEnemyTank1Right;
				break;
			}
		}else{									//敌人坦克2
			switch(tankDir){
			case 1:
				bmpTank=bmpEnemyTank2Up;
				break;
			case 2:
				bmpTank=bmpEnemyTank2Down;
				break;
			case 3:
				bmpTank=bmpEnemyTank2Left;
				break;
			case 4:
				bmpTank=bmpEnemyTank2Right;
				break;
			}
		}
	}
	public void drawTank(Canvas canvas){		//在屏幕上显示这个坦克
		tankX=tankRow*10;		//坦克的坐标
		tankY=tankLine*10;
		getBitmap();
		canvas.drawBitmap(bmpTank, tankX,tankY,null);
	}
}
