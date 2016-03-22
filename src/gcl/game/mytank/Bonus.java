package gcl.game.mytank;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

//奖品类
public class Bonus {
	GameView gv;
	int type;	//奖品类型，代码对应着功能：1-炸弹；2-定时器；3-油箱；4-盾版（无敌）；5-轮船；6-铲子；7-星星；8-坦克
	int bonusLine,bonusRow;		//该奖品所在的行和列号，每个奖品都是10*10像素大小
	int bonusX,bonusY;			//该奖品的左上角所在的左上角的坐标
	int liveLongTime;			//存活的时间，按次算，每次都是1秒，例如该值为97,意味着该Bonus已经存在了97秒，120秒后要收回该Bonus，以便产生新的Bonus
	Bitmap bmpBonusBomb1,bmpBonusBomb2,bmpBonusClock1,bmpBonusClock2,bmpBonusFuel1,bmpBonusFuel2;
	Bitmap bmpBonusShield1,bmpBonusShield2,bmpBonusShip1,bmpBonusShip2,bmpBonusShovel1,bmpBonusShovel2;
	Bitmap bmpBonusStar1,bmpBonusStar2,bmpBonusTank1,bmpBonusTank2;
	Bitmap bmpBonus;			//当前奖品显示图片，要根据type来定
	int status;					//当前奖品的状态，1表示该类型的第一张图片，2表示该类型的第二张图片
	Resources r;
	boolean bonusSwitchStatusFlag;
	int bonusSwitchStatusSleepSpan=1000;			//每1000毫秒更换一次图片，造成闪烁的效果
	public Bonus(final GameView gv,int tp,int bl,int br){
		this.gv=gv;
		this.type=tp;
		this.bonusLine=bl;
		this.bonusRow=br;
		status=1;
		liveLongTime=0;
		bonusSwitchStatusFlag=true;
		initBitmap();
		new Thread(){								//这个线程控制Bonus的闪烁，同时控制它的存活时间：2分钟
			public void run() {
				while(bonusSwitchStatusFlag){
					try{
						Thread.sleep(bonusSwitchStatusSleepSpan);
					}catch(Exception e){
						e.printStackTrace();
					}
					status=( status==1 ? 2:1);		//让status在1和2之间进行切换
					liveLongTime++;					//存活时间加1
					if(liveLongTime>=120){
						gv.ta.myHandler.sendEmptyMessage(400);	//发送一个400号消息，以便收回该Bonus
						bonusSwitchStatusFlag=false;
					}
				}
			};
		}.start();
	}
	public void initBitmap(){
		r=gv.getResources();
		bmpBonusBomb1=BitmapFactory.decodeResource(r, R.drawable.bonusbomb1);
		bmpBonusBomb2=BitmapFactory.decodeResource(r, R.drawable.bonusbomb2);
		bmpBonusClock1=BitmapFactory.decodeResource(r, R.drawable.bonusclock1);
		bmpBonusClock2=BitmapFactory.decodeResource(r, R.drawable.bonusclock2);
		bmpBonusFuel1=BitmapFactory.decodeResource(r, R.drawable.bonusfuel1);
		bmpBonusFuel2=BitmapFactory.decodeResource(r, R.drawable.bonusfuel2);
		bmpBonusShield1=BitmapFactory.decodeResource(r, R.drawable.bonusshield1);
		bmpBonusShield2=BitmapFactory.decodeResource(r, R.drawable.bonusshield2);
		bmpBonusShip1=BitmapFactory.decodeResource(r, R.drawable.bonusship1);
		bmpBonusShip2=BitmapFactory.decodeResource(r, R.drawable.bonusship2);
		bmpBonusShovel1=BitmapFactory.decodeResource(r, R.drawable.bonusshovel1);
		bmpBonusShovel2=BitmapFactory.decodeResource(r, R.drawable.bonusshovel2);
		bmpBonusStar1=BitmapFactory.decodeResource(r, R.drawable.bonusstar1);
		bmpBonusStar2=BitmapFactory.decodeResource(r, R.drawable.bonusstar2);
		bmpBonusTank1=BitmapFactory.decodeResource(r, R.drawable.bonustank1);
		bmpBonusTank2=BitmapFactory.decodeResource(r, R.drawable.bonustank2);
	}
	public void getBonusBitmap(){	//根据奖品的型号、状态来确定当前所用的图片
		switch(type){
		case 1:
			if(status==1)
				bmpBonus=bmpBonusBomb1;
			else
				bmpBonus=bmpBonusBomb2;
			break;
		case 2:
			if(status==1)
				bmpBonus=bmpBonusClock1;
			else
				bmpBonus=bmpBonusClock2;
			break;
		case 3:
			if(status==1)
				bmpBonus=bmpBonusFuel1;
			else
				bmpBonus=bmpBonusFuel2;
			break;
		case 4:
			if(status==1)
				bmpBonus=bmpBonusShield1;
			else
				bmpBonus=bmpBonusShield2;
			break;
		case 5:
			if(status==1)
				bmpBonus=bmpBonusShip1;
			else
				bmpBonus=bmpBonusShip2;
			break;
		case 6:
			if(status==1)
				bmpBonus=bmpBonusShovel1;
			else
				bmpBonus=bmpBonusShovel2;
			break;
		case 7:
			if(status==1)
				bmpBonus=bmpBonusStar1;
			else
				bmpBonus=bmpBonusStar2;
			break;
		case 8:
			if(status==1)
				bmpBonus=bmpBonusTank1;
			else
				bmpBonus=bmpBonusTank2;
			break;
		}
	}
	public void displayBonus(Canvas canvas){
		bonusX=bonusRow*10;									//计算该Bonus的坐标
		bonusY=bonusLine*10;
		getBonusBitmap();									//获取当前Bonus的图片
		canvas.drawBitmap(bmpBonus, bonusX,bonusY, null);	//显示图片
	}
}
