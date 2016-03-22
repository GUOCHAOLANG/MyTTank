package gcl.game.mytank;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

//子弹类，所有的子弹都是从该类实例出来的对象
public class Bullet {
	GameView gv;
	int enemyOrFriend;				//用来标识该子弹是我方坦克发出的还是敌人坦克发出的子弹，1表示敌人，0表示自己
	int bulletType;					//用来标识子弹的型号，1或2号，其中2号攻击力更强
	int bulletLine,bulletRow;		//子弹所处的行号和列号
	int bulletX,bulletY;			//子弹的坐标，这个坐标可以由上面的行号和列号计算出来
	int bulletDir;					//子弹飞行的方向，与坦克发出子弹时的朝向一致，1、2、3、4分别代表上、下、左、右
	int bulletSleepSpan;			//子弹每飞一步（就是向前走一格）后睡眠的时间，这个时间越短，则飞行速度越快
	boolean bulletFlag;
	Resources r;
	Bitmap bmpBullet;				//第一种子弹图片为3*4像素，第二种子弹图片为6*8像素
	Bitmap bmpMyBullet1Up,bmpMyBullet1Down,bmpMyBullet1Left,bmpMyBullet1Right;	//我方第一种子弹上下左右图片
	Bitmap bmpMyBullet2Up,bmpMyBullet2Down,bmpMyBullet2Left,bmpMyBullet2Right;	//我方第二种子弹上下左右图片
	Bitmap bmpEnemyBullet1Up,bmpEnemyBullet1Down,bmpEnemyBullet1Left,bmpEnemyBullet1Right;	//敌方第一种子弹上下左右图片
	Bitmap bmpEnemyBullet2Up,bmpEnemyBullet2Down,bmpEnemyBullet2Left,bmpEnemyBullet2Right;	//敌方第二种子弹上下左右图片
	public Bullet(final GameView gv,int eOf,int bt,int bl,int br,int bd,int bss){
		this.gv=gv;
		enemyOrFriend=eOf;
		bulletType=bt;
		bulletLine=bl;
		bulletRow=br;
		bulletDir=bd;
		bulletSleepSpan=bss;
		bulletFlag=true;
		initBitmap();
		new Thread(){				//子弹自动飞行线程
			public void run() {
				while(bulletFlag){
					try{				//
						Thread.sleep(bulletSleepSpan);
					}catch(Exception e){
						e.printStackTrace();
					}
					synchronized(gv.myBullets){		//线程同步，防止多个线程同时去操作这个链表
						if((bulletLine==38 && bulletRow==15) || (bulletLine==38 && bulletRow==16) ||
								(bulletLine==39 && bulletRow==15) || (bulletLine==39 && bulletRow==16)){	//城堡被炸
							gv.maps[38][15]=0;		//城堡消失
							bulletFlag=false;
							if(enemyOrFriend==0)	//如果是我方子弹，发送100号 消息
								gv.ta.myHandler.sendEmptyMessage(100);	//发送100号消息，从而可以真正地销毁当前这个子弹
							else
								gv.ta.myHandler.sendEmptyMessage(200);	//发送200消息，销毁敌人子弹
							gv.ta.myHandler.sendEmptyMessage(300);		//发送300消息，GameOver了
						}
						//检测我方子弹是否与某棵敌人的子弹相遇，如果相遇，则两个子弹都销毁
						if(enemyOrFriend==0){
							synchronized(gv.enemyBullets){
								int i;
								for(i=0;i<gv.enemyBullets.size();i++){
									Bullet one=gv.enemyBullets.get(i);
									if(bulletLine==one.bulletLine && bulletRow==one.bulletRow){
										bulletFlag=false;
										gv.ta.myHandler.sendEmptyMessage(100);		//销毁我方子弹
										one.bulletFlag=false;
										gv.ta.myHandler.sendEmptyMessage(200);		// 销毁敌人的这棵子弹
										break;
									}
								}
							}
						}
						//下面根据方向确定飞行线路，并检测子弹是否遇到障碍或坦克
						switch(bulletDir){		//根据方向确定飞行线路
						case 1:					//向上
							if(bulletLine<=0){	//子弹飞出了屏幕上方，应该销毁这个子弹
								bulletFlag=false;
								if(enemyOrFriend==0)	//如果是我方子弹，发送100号 消息
									gv.ta.myHandler.sendEmptyMessage(100);	//发送100号消息，从而可以真正地销毁当前这个子弹
								else
									gv.ta.myHandler.sendEmptyMessage(200);	//发送200消息，销毁敌人子弹
							}else{
								boolean hasCrash=false;
								if(bulletLine>=1){						//检测子弹是否打到了墙或金刚石
									if(gv.maps[bulletLine-1][bulletRow]==3){//如果子弹上方是墙，则消除之
										gv.maps[bulletLine-1][bulletRow]=0;	//墙块变为0，就消掉了这块墙
										hasCrash=true;						//标志为true，从而可以销毁这个子弹
									}
									if(gv.maps[bulletLine-1][bulletRow+1]==3){
										gv.maps[bulletLine-1][bulletRow+1]=0;
										hasCrash=true;
									}
									if(gv.maps[bulletLine-1][bulletRow]==4 || gv.maps[bulletLine-1][bulletRow+1]==4){//金刚石
										hasCrash=true;						//金刚石，直接销毁这个子弹
									}
								}
								if(bulletLine>=2){						//检测子弹是否打到了敌人的坦克
									if(enemyOrFriend==0){				//我方子弹与敌人的坦克是否相遇
										synchronized(gv.enemyTanks){
											int i;
											for(i=0;i<gv.enemyTanks.size();i++){
												OneTank one=gv.enemyTanks.get(i);
												if(one!=null){
													if(one.tankLine==bulletLine-2 && (one.tankRow==bulletRow-1 ||one.tankRow==bulletRow || one.tankRow==bulletRow+1)){ //上方有坦克被打中
														hasCrash=true;
														one.tankBlood-=bulletType==1?250:500;	//敌人坦克减去一定血
														Log.d("敌人剩余血量",""+one.tankBlood);
														if(one.tankBlood<=0){					//敌人坦克被消灭了一个
															one.enemyFlag=false;
															one.enemyFireFlag=false;
															gv.ta.myHandler.sendEmptyMessage(101);	//发送101消息
														}
													}
												}
											}
										}
									}else{								//检测敌人坦克是否打中了我方坦克
										synchronized(gv.myTank){
											OneTank one=gv.myTank;
											if(one!=null){
												if(one.tankLine==bulletLine-2 && (one.tankRow==bulletRow-1 ||one.tankRow==bulletRow || one.tankRow==bulletRow+1)){ //上方有坦克被打中
													hasCrash=true;
													one.tankBlood-=bulletType==1?250:500;	//我方坦克减去一定血
													Log.d("我方剩余血量",""+one.tankBlood);
													if(one.tankBlood<=0){					//我方坦克被消灭了一个
														gv.ta.myHandler.sendEmptyMessage(201);	//发送201消息
													}
												}
											}
										}
									}
								}
								if(hasCrash==true){
									bulletFlag=false;
									if(enemyOrFriend==0){
										gv.ta.myHandler.sendEmptyMessage(100);
									}else{
										gv.ta.myHandler.sendEmptyMessage(200);
									}
								}
							}
							bulletLine--;
							break;
						case 2:					//向下
							if(bulletLine>=38){	//子弹飞出了屏幕下方，应该销毁这个子弹
								bulletFlag=false;
								if(enemyOrFriend==0){
									gv.ta.myHandler.sendEmptyMessage(100);
								}else{
									gv.ta.myHandler.sendEmptyMessage(200);
								}
							}else if(bulletLine<=37){
								boolean hasCrash=false;
								//先检测是否与墙、金刚石等碰撞
								if(gv.maps[bulletLine+2][bulletRow]==3){//如果子弹下方是墙，则消除之
									gv.maps[bulletLine+2][bulletRow]=0;	//墙块变为0，就消掉了这块墙
									hasCrash=true;						//标志为true，从而可以销毁这个子弹
								}
								if(gv.maps[bulletLine+2][bulletRow+1]==3){
									gv.maps[bulletLine+2][bulletRow+1]=0;
									hasCrash=true;
								}
								if(gv.maps[bulletLine+2][bulletRow]==4 || gv.maps[bulletLine+2][bulletRow+1]==4){//金刚石
									hasCrash=true;						//金刚石，直接销毁这个子弹
								}
								//下面检测是否与敌人的坦克碰撞
								if(enemyOrFriend==0){					//我方子弹
									synchronized(gv.enemyTanks){
										int i;
										for(i=0;i<gv.enemyTanks.size();i++){
											OneTank one=gv.enemyTanks.get(i);
											//如果下方三个位置上有坦克，则让该坦克减一定的血，并消除该子弹
											if(one.tankLine==bulletLine+1 &&(one.tankRow==bulletRow-1 || one.tankRow==bulletRow || one.tankRow==bulletRow+1)){
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//敌人坦克减去一定血
												Log.d("敌人剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//敌人坦克被消灭了一个
													one.enemyFlag=false;
													one.enemyFireFlag=false;
													gv.ta.myHandler.sendEmptyMessage(101);	//发送101消息
												}
											}
										}
									}
								}else{
									synchronized(gv.myTank){
										OneTank one=gv.myTank;
										if(one!=null){
											if(one.tankLine==bulletLine+1 &&(one.tankRow==bulletRow-1 || one.tankRow==bulletRow || one.tankRow==bulletRow+1)){ //下方有坦克被打中
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//我方坦克减去一定血
												Log.d("我方剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//我方坦克被消灭了一个
													gv.ta.myHandler.sendEmptyMessage(201);	//发送201消息
												}
											}
										}
									}
								}
								if(hasCrash==true){
									bulletFlag=false;
									if(enemyOrFriend==0){
										gv.ta.myHandler.sendEmptyMessage(100);
									}else{
										gv.ta.myHandler.sendEmptyMessage(200);
									}
								}
							}
							bulletLine++;
							break;
						case 3:					//向左
							if(bulletRow<=0){	//子弹飞出了屏幕左方
								bulletFlag=false;
								if(enemyOrFriend==0){
									gv.ta.myHandler.sendEmptyMessage(100);
								}else{
									gv.ta.myHandler.sendEmptyMessage(200);
								}
							}else if(bulletRow>=1){
								boolean hasCrash=false;
								if(gv.maps[bulletLine][bulletRow-1]==3){	//子弹左方是墙
									gv.maps[bulletLine][bulletRow-1]=0;
									hasCrash=true;
								}
								if(gv.maps[bulletLine+1][bulletRow-1]==3){
									gv.maps[bulletLine+1][bulletRow-1]=0;
									hasCrash=true;
								}
								if(gv.maps[bulletLine][bulletRow-1]==4 || gv.maps[bulletLine+1][bulletRow-1]==4){//金刚石
									hasCrash=true;						//金刚石，直接销毁这个子弹
								}
								if(enemyOrFriend==0){
									synchronized(gv.enemyTanks){
										int i;
										for(i=0;i<gv.enemyTanks.size();i++){
											OneTank one=gv.enemyTanks.get(i);
											//如果左方三个位置上有坦克，则让该坦克减一定的血，并消除该子弹
											if(one.tankRow==bulletRow-2 &&(one.tankLine==bulletLine-1 || one.tankLine==bulletLine || one.tankLine==bulletLine+1)){
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//敌人坦克减去一定血
												Log.d("敌人剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//敌人坦克被消灭了一个
													one.enemyFlag=false;
													one.enemyFireFlag=false;
													gv.ta.myHandler.sendEmptyMessage(101);	//发送101消息
												}
											}
										}
									}
								}else{
									synchronized(gv.myTank){
										OneTank one=gv.myTank;
										if(one!=null){
											if(one.tankRow==bulletRow-2 &&(one.tankLine==bulletLine-1 || one.tankLine==bulletLine || one.tankLine==bulletLine+1)){ //左方有坦克被打中
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//我方坦克减去一定血
												Log.d("我方剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//我方坦克被消灭了一个
													gv.ta.myHandler.sendEmptyMessage(201);	//发送201消息
												}
											}
										}
									}
								}
								if(hasCrash==true){
									bulletFlag=false;
									if(enemyOrFriend==0){
										gv.ta.myHandler.sendEmptyMessage(100);
									}else{
										gv.ta.myHandler.sendEmptyMessage(200);
									}
								}
							}
							bulletRow--;
							break;
						case 4:					//向右
							if(bulletRow>=31){	//子弹飞出了屏蔽右方
								bulletFlag=false;
								if(enemyOrFriend==0){
									gv.ta.myHandler.sendEmptyMessage(100);
								}else{
									gv.ta.myHandler.sendEmptyMessage(200);
								}
							}else if(bulletRow<=29){
								boolean hasCrash=false;
								if(gv.maps[bulletLine][bulletRow+2]==3){	//子弹右方是墙
									gv.maps[bulletLine][bulletRow+2]=0;
									hasCrash=true;
								}
								if(gv.maps[bulletLine+1][bulletRow+2]==3){
									gv.maps[bulletLine+1][bulletRow+2]=0;
									hasCrash=true;
								}
								if(gv.maps[bulletLine][bulletRow+2]==4 || gv.maps[bulletLine+1][bulletRow+2]==4){//金刚石
									hasCrash=true;						//金刚石，直接销毁这个子弹
								}
								if(enemyOrFriend==0){
									synchronized(gv.enemyTanks){
										int i;
										for(i=0;i<gv.enemyTanks.size();i++){
											OneTank one=gv.enemyTanks.get(i);
											//如果右方三个位置上有坦克，则让该坦克减一定的血，并消除该子弹
											if(one.tankRow==bulletRow+1 &&(one.tankLine==bulletLine-1 || one.tankLine==bulletLine || one.tankLine==bulletLine+1)){
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//敌人坦克减去一定血
												Log.d("敌人剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//敌人坦克被消灭了一个
													one.enemyFlag=false;
													one.enemyFireFlag=false;
													gv.ta.myHandler.sendEmptyMessage(101);	//发送101消息
												}
											}
										}
									}
								}else{
									synchronized(gv.myTank){
										OneTank one=gv.myTank;
										if(one!=null){
											if(one.tankRow==bulletRow+1 &&(one.tankLine==bulletLine-1 || one.tankLine==bulletLine || one.tankLine==bulletLine+1)){ //右方有坦克被打中
												hasCrash=true;
												one.tankBlood-=bulletType==1?250:500;	//我方坦克减去一定血
												Log.d("我方剩余血量",""+one.tankBlood);
												if(one.tankBlood<=0){					//我方坦克被消灭了一个
													gv.ta.myHandler.sendEmptyMessage(201);	//发送201消息
												}
											}
										}
									}
								}
								if(hasCrash==true){
									bulletFlag=false;
									if(enemyOrFriend==0){
										gv.ta.myHandler.sendEmptyMessage(100);
									}else{
										gv.ta.myHandler.sendEmptyMessage(200);
									}
								}
							}
							bulletRow++;
							break;
						}
					}
				}
			};			
		}.start();
	}
	public void initBitmap(){		//初始化所有的子弹图片
		r=gv.getResources();
		bmpMyBullet1Up=BitmapFactory.decodeResource(r, R.drawable.mybullet1up);
		bmpMyBullet1Down=BitmapFactory.decodeResource(r, R.drawable.mybullet1down);
		bmpMyBullet1Left=BitmapFactory.decodeResource(r, R.drawable.mybullet1left);
		bmpMyBullet1Right=BitmapFactory.decodeResource(r, R.drawable.mybullet1right);
		bmpMyBullet2Up=BitmapFactory.decodeResource(r, R.drawable.mybullet2up);
		bmpMyBullet2Down=BitmapFactory.decodeResource(r, R.drawable.mybullet2down);
		bmpMyBullet2Left=BitmapFactory.decodeResource(r, R.drawable.mybullet2left);
		bmpMyBullet2Right=BitmapFactory.decodeResource(r, R.drawable.mybullet2right);
		bmpEnemyBullet1Up=BitmapFactory.decodeResource(r, R.drawable.enemybullet1up);
		bmpEnemyBullet1Down=BitmapFactory.decodeResource(r, R.drawable.enemybullet1down);
		bmpEnemyBullet1Left=BitmapFactory.decodeResource(r, R.drawable.enemybullet1left);
		bmpEnemyBullet1Right=BitmapFactory.decodeResource(r, R.drawable.enemybullet1right);
		bmpEnemyBullet2Up=BitmapFactory.decodeResource(r, R.drawable.enemybullet2up);
		bmpEnemyBullet2Down=BitmapFactory.decodeResource(r, R.drawable.enemybullet2down);
		bmpEnemyBullet2Left=BitmapFactory.decodeResource(r, R.drawable.enemybullet2left);
		bmpEnemyBullet2Right=BitmapFactory.decodeResource(r, R.drawable.enemybullet2right);
	}
	public void getBulletBitmap(){				//根据当前子弹的型号、方向、敌我来确定用哪副图片
		if(enemyOrFriend==0){					//我方子弹
			if(bulletType==1){					//型号为1
				switch(bulletDir){				//根据方向
				case 1:
					bmpBullet=bmpMyBullet1Up;
					break;
				case 2:
					bmpBullet=bmpMyBullet1Down;
					break;
				case 3:
					bmpBullet=bmpMyBullet1Left;
					break;
				case 4:
					bmpBullet=bmpMyBullet1Right;
					break;
				}
			}else{								//型号为2
				switch(bulletDir){				//根据方向
				case 1:
					bmpBullet=bmpMyBullet2Up;
					break;
				case 2:
					bmpBullet=bmpMyBullet2Down;
					break;
				case 3:
					bmpBullet=bmpMyBullet2Left;
					break;
				case 4:
					bmpBullet=bmpMyBullet2Right;
					break;
				}
			}
		}else{									//敌人子弹
			if(bulletType==1){					//型号为1
				switch(bulletDir){				//根据方向
				case 1:
					bmpBullet=bmpEnemyBullet1Up;
					break;
				case 2:
					bmpBullet=bmpEnemyBullet1Down;
					break;
				case 3:
					bmpBullet=bmpEnemyBullet1Left;
					break;
				case 4:
					bmpBullet=bmpEnemyBullet1Right;
					break;
				}
			}else{								//型号为2
				switch(bulletDir){				//根据方向
				case 1:
					bmpBullet=bmpEnemyBullet2Up;
					break;
				case 2:
					bmpBullet=bmpEnemyBullet2Down;
					break;
				case 3:
					bmpBullet=bmpEnemyBullet2Left;
					break;
				case 4:
					bmpBullet=bmpEnemyBullet2Right;
					break;
				}
			}
		}
	}
	public void displayBullet(Canvas canvas){	//在屏幕上显示子弹
		//如何计算子弹的坐标呢：根据子弹所处的方向、行号、列号来计算
		switch(bulletDir){						//方向
		case 1:									//向上
			bulletX=(bulletRow+1)*10-(bulletType==1?2:3);
			bulletY=bulletLine*10-(bulletType==1?4:8);
			break;
		case 2:									//向下
			bulletX=(bulletRow+1)*10-(bulletType==1?2:3);
			bulletY=(bulletLine+2)*10+(bulletType==1?4:8);
			break;
		case 3:									//向左
			bulletX=bulletRow*10-(bulletType==1?4:8);
			bulletY=(bulletLine+1)*10-(bulletType==1?2:4);
			break;
		case 4:									//向右
			bulletX=(bulletRow+2)*10;
			bulletY=(bulletLine+1)*10-(bulletType==1?2:4);
			break;
		}
		getBulletBitmap();						//调用函数，获取当前子弹应该绘制的图片
		canvas.drawBitmap(bmpBullet, bulletX,bulletY, null);	//绘制子弹
	}
}
