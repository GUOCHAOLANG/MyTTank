package gcl.game.mytank;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GameView extends View { 
	TankActivity ta;
	int maps[][];					//场景地图数组
	int lines,rows;					//整个场景地图的行和列数
	Paint bigBlueRectPaint,smallBlackRectPaint;	//下面写文字用的两个矩形Paint
	Paint strCopyrightPaint;
	String strCopyright="版权所有：guochaolang";
	Bitmap bmpBackground;
	Bitmap bmpGrass,bmpRiver,bmpWall,bmpDiamond,bmpBao;
	Resources r;
	OneTank myTank;					//我方坦克
	int nMyTanks;					//我方还剩下多少个坦克，一般初始为3个，包括正在战斗的那个
	List <OneTank> enemyTanks;		//敌人坦克链表
	List <Bullet> myBullets;		//我方坦克发出的子弹链表
	List <Bullet> enemyBullets;		//敌人坦克发出的子弹链表
	int maxEnemyTanks;				//敌人坦克最大的数目（一般为20个）
	int leftEnemyTanks;				//敌人坦克目前还剩下多少个（每打死一个敌人坦克，该数目-1）
	int displayEnemyTanks;			//显示多少个敌人的坦克（一般为6个，但是如果敌人的坦克数目少于6,则显示真实数目）
	int gameViewSleepSpan;
	boolean hasABonus;				//表示目前是否已经有一个Bonus？如果有的话，则不再产生
	Bonus oneBonus;					//Bonus对象
	boolean gameViewFlag;
	boolean hasGameOver;			//是否Game Over;
	Paint textMyTanksPaint,textEnemyTanksPaint,textMyTankBloodPaint;	//我方坦克数、敌人坦克数、我方坦克血量的文字Paint
	Paint textNowLevelPaint;		//显示现在第几关
	Paint gameOverPaint;
	public GameView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		ta=(TankActivity)context;
		nMyTanks=3;					//我方坦克数：初始化为3个
		initBitmap();				//调用方法加载图片
		initMaps();					//调用方法加载场景地图数组
		initPaint();
		myBullets=new LinkedList<Bullet>();				//初始化我方坦克发出的子弹链表
		enemyBullets=new LinkedList<Bullet>();			//初始化敌人子弹链表
		initMyTank();
		initEnemyTank();
		hasGameOver=false;
		gameViewFlag=true;
		hasABonus=false;
		gameViewSleepSpan=50;
		new Thread(){
			public void run() {
				while(gameViewFlag){
					ta.myHandler.sendEmptyMessage(5);		//定期发送5号消息，从而更新屏幕
					try{
						Thread.sleep(gameViewSleepSpan);	//睡眠50毫秒
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	public void initMyTank()		//初始化我方坦克
	{
		myTank=new OneTank(this,0,38,11,4,1,1,1000,50);
	}
	public void initEnemyTank(){	//初始化敌人坦克
		maxEnemyTanks=20;
		leftEnemyTanks=20;
		displayEnemyTanks=6;
		enemyTanks=new LinkedList<OneTank>();		//初始化链表，这样enemyTanks就是一个链表了，可以插入add、删除、读取get

		int i;
		for(i=0;i<displayEnemyTanks;i++){			//先初始化6个敌人坦克
			int tankLine,tankRow=0;
			tankLine=0;
			switch(i%3){	//根据坦克出生的位置编号来确定其行和列
			case 0:			//第0、3、6、9等个
				tankRow=0;	//第0列
				break;
			case 1:			//第1、4、7、10等个
				tankRow=15;	//第15列
				break;
			case 2:			//第2、5、8、11等个
				tankRow=30;	//第30列
				break;
			}
			int sleepSpan=(int)(Math.random()*3)*200+400;
			OneTank one=new OneTank(this,i%2+1,tankLine,tankRow,i%3,2,1,(i%2+1)==1?1000:500,sleepSpan);
			enemyTanks.add(one);					//将这个坦克插入链表中
		}
	}
	public void initPaint(){
		bigBlueRectPaint=new Paint();
		bigBlueRectPaint.setColor(Color.BLUE);
		smallBlackRectPaint=new Paint();
		smallBlackRectPaint.setColor(Color.BLACK);
		strCopyrightPaint=new Paint();
		strCopyrightPaint.setColor(Color.YELLOW);	//黄色字体
		strCopyrightPaint.setTextSize(16);			//16号字体
		textMyTanksPaint=new Paint();
		textMyTanksPaint.setColor(Color.RED);
		textMyTanksPaint.setTextSize(12);
		textEnemyTanksPaint=new Paint();
		textEnemyTanksPaint.setColor(Color.GREEN);
		textEnemyTanksPaint.setTextSize(12);
		textMyTankBloodPaint=new Paint();
		textMyTankBloodPaint.setColor(Color.CYAN);
		textMyTankBloodPaint.setTextSize(12);
		gameOverPaint=new Paint();
		gameOverPaint.setColor(Color.BLACK);
		gameOverPaint.setTextSize(50);
		textNowLevelPaint=new Paint();
		textNowLevelPaint.setColor(Color.WHITE);
		textNowLevelPaint.setTextSize(16);
	}
	public void initMaps(){
		int i,j;
		lines=TankMaps.maps[ta.level].length;		//取到第ta.level关的行数（当然我们每一关的行数都固定为40行
		rows=TankMaps.maps[ta.level][0].length;		//取到第ta.level关的列数（固定为32列）
		maps=new int[lines][rows];
		for(i=0;i<lines;i++){
			for(j=0;j<rows;j++){
				maps[i][j]=TankMaps.maps[ta.level][i][j];	//将第ta.level关的地图数据拷贝到当前数组中
			}
		}
	}
	public void initBitmap(){
		r=getResources();
		bmpGrass=BitmapFactory.decodeResource(r, R.drawable.grass);
		bmpRiver=BitmapFactory.decodeResource(r, R.drawable.river);
		bmpWall=BitmapFactory.decodeResource(r, R.drawable.wall);
		bmpBao=BitmapFactory.decodeResource(r, R.drawable.bao);
		bmpDiamond=BitmapFactory.decodeResource(r,R.drawable.diamond);
		bmpBackground=BitmapFactory.decodeResource(r, R.drawable.backgroundbmp);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(bmpBackground,0,0,null);				//显示背景图片,进行整体刷新
		drawMaps(canvas);										//调用方法绘制地图中各种物体,如草、河、墙等
		drawRect(canvas);
		drawTextInformation(canvas);
		myTank.drawTank(canvas);								//显示我方坦克
		drawEnemyTanks(canvas);									//显示敌人坦克
		drawMyBullets(canvas);									//显示我方子弹
		drawEnemyBullets(canvas);								//显示敌人子弹
		drawGameOver(canvas);									//必要时刻显示"Game Over"
		drawBonus(canvas);										//显示Bonus
	}
	protected void drawBonus(Canvas canvas){					//如果当前有Bonus，则打印之
		if(hasABonus==true && oneBonus!=null){
			oneBonus.displayBonus(canvas);
		}
	}
	protected void drawEnemyTanks(Canvas canvas){				//显示所有的敌人的坦克
		int i;
		OneTank one;
		int nEnemyTanks=enemyTanks.size();						//获取敌方坦克链表中的元素个数
		for(i=0;i<nEnemyTanks;i++){
			one=enemyTanks.get(i);								//读取第i个坦克
			one.drawTank(canvas);								//显示这个坦克
		}
	}
	protected void drawRect(Canvas canvas){						//绘制下面的两个矩形区域，用来输出各种文字信息
		canvas.drawRect(0,400,320,480, bigBlueRectPaint);
		canvas.drawRect(5,405,315,475, smallBlackRectPaint);
	}
	protected void drawTextInformation(Canvas canvas){			//显示各种文字信息
		canvas.drawText("我方坦克数："+nMyTanks, 10, 420, textMyTanksPaint);
		canvas.drawText("坦克血量："+myTank.tankBlood, 10, 435, textMyTankBloodPaint);
		canvas.drawText("敌人坦克数："+leftEnemyTanks, 10, 450, textEnemyTanksPaint);
		canvas.drawText(strCopyright, 10,470,strCopyrightPaint);
		canvas.drawText("现在第"+(int)(ta.level+1)+"关", 160, 420, textNowLevelPaint);
	}
	protected void drawGameOver(Canvas canvas){
		if(nMyTanks<=0 || hasGameOver){
			canvas.drawText("Game Over", 50, 200, gameOverPaint);
		}
	}
	protected void drawMyBullets(Canvas canvas){				//绘制出我主所有的子弹
		int i;
		int nBullets=myBullets.size();							//目前我方一共发出了nBullets个子弹
		for(i=0;i<nBullets;i++){								//一个一个地绘制
			Bullet one=myBullets.get(i);
			if(one!=null)
				one.displayBullet(canvas);
		}
	}
	protected void drawEnemyBullets(Canvas canvas){				//在屏幕上绘制出敌人所有的子弹
		int i;
		int nBullets=enemyBullets.size();						//目前敌人一共发出了nBullets个子弹
		for(i=0;i<nBullets;i++){								//一个一个地绘制
			Bullet one=enemyBullets.get(i);
			if(one!=null)
				one.displayBullet(canvas);
		}
	}
	protected void drawMaps(Canvas canvas){						//绘制场景地图的各种物体 
		int i,j;
		int X,Y;												//计算出该物体应该绘制的位置
		for(i=0;i<lines;i++){
			for(j=0;j<rows;j++){
				X=10*j;
				Y=10*i;
				switch(maps[i][j]){
				case 0:											//空白,无绘制
					break;
				case 1:											//绘制草
					canvas.drawBitmap(bmpGrass,X,Y , null);
					break;
				case 2:											//绘制河
					canvas.drawBitmap(bmpRiver,X,Y , null);
					break;
				case 3:											//绘制墙
					canvas.drawBitmap(bmpWall,X,Y , null);
					break;
				case 4:											//绘制金刚石
					canvas.drawBitmap(bmpDiamond,X,Y , null);
					break;
				case 5:											//绘制城堡宝物
					canvas.drawBitmap(bmpBao,X,Y , null);
					break;
				default:
					break;
				}
			}
		}		
	}
}
