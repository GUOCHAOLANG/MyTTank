package gcl.game.mytank;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class TankActivity extends Activity {
    /** Called when the activity is first created. */
	private static final int ITEM_RESTART=Menu.FIRST;	//重新开始
	private static final int ITEM_EXIT=Menu.FIRST+1;	//退出游戏
	private static final int ITEM_PAUSE=Menu.FIRST+2;	//暂停游戏
	int level;					//表示用户当前正处在第几关（0开始）
	int keyCode;				//记录用户的按键，上下左右的代码分别是：19,20,21,22
	GameView gv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//设置不显示标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);	//设置全屏显示
        level=0;				//一开始是第0关
        gv=new GameView(this);
        setContentView(gv);
    }
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
    	this.keyCode=keyCode;
    	Log.d("KeyCode", ""+keyCode);
    	return false;
    };
    public boolean onCreateOptionsMenu(Menu menu) {		//Android创建菜单时自动调用
    	menu.add(0,ITEM_RESTART,0,"���¿�ʼ");			//添加几个菜单项
    	menu.add(0,ITEM_PAUSE,0,"��ͣ��Ϸ");
    	menu.add(0,ITEM_EXIT,0,"�˳�");
    	return true;
    };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//参数item就是用户选中了某个菜单项
    	// TODO Auto-generated method stub
    	switch(item.getItemId()){		//根据用户选择了哪个项目，读出其ID号，来进行相应的处理
    	case ITEM_RESTART:				//重新开始项    		
    		myHandler.sendEmptyMessage(1);	//发送1号消息
    		break;
    	case ITEM_PAUSE:				//暂停游戏项
    		break;
    	case ITEM_EXIT:					//退出程序项
    		myHandler.sendEmptyMessage(0);	//发送0号消息，退出程序
    		break;
    	}
//    	return super.onOptionsItemSelected(item);
    	return true;			//return true的意思是，这些菜单项我已经处理好了，不用劳驾Android来处理
    }
    public boolean onTouchEvent(android.view.MotionEvent event) {	//处理屏幕按键
    	int X,Y;				//用户手指在屏幕上按的点的坐标
    	X=(int)event.getX();
    	Y=(int)event.getY();
    	if(Y<100)				//向上键
    		keyCode=19;
    	else if(Y>300 && Y<400)	//向下键 
    		keyCode=20;
    	if(X<100 && Y>100 && Y<300 )	//向左
    		keyCode=21;
    	if(X>219 && Y>100 && Y<300 )	//向右
    		keyCode=22;
    	if(Y>400)						//发子弹
    		keyCode=62;
    	return false;
    };
    Handler myHandler=new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		int i;
    		switch(msg.what){
    		case 0:								//0号消息
    			System.exit(0);					//退出程序
    			break;
    		case 1:								//1号消息，重新开始游戏
    			gv.gameViewFlag=false;			//先让刷新屏幕的线程停下来
    			for(i=0;i<gv.myBullets.size();i++){	//让所有的我方子弹都销毁掉
    				Bullet one=gv.myBullets.get(i);
    				one.bulletFlag=false;
    				gv.myBullets.remove(i);
    			}
    			for(i=0;i<gv.enemyBullets.size();i++){	//让所有的敌人子弹都销毁掉
    				Bullet one=gv.enemyBullets.get(i);
    				one.bulletFlag=false;
    				gv.enemyBullets.remove(i);
    			}
    			gv.myTank.myFlag=gv.myTank.enemyFireFlag=gv.myTank.enemyFlag=false;	//销毁我方坦克
    			for(i=0;i<gv.enemyTanks.size();i++){			//销毁所有的敌人的坦克
    				OneTank one=gv.enemyTanks.get(i);
    				one.myFlag=one.enemyFireFlag=one.enemyFlag=false;
    				gv.enemyTanks.remove(i);
    			}
    			gv=new GameView(TankActivity.this);				//重新开始游戏
    	        setContentView(gv);
    			break;
    		case 2:								//2号消息
    			break;
    		case 3:								//3号消息
    			break;
    		case 4:								//4号消息
    			break;
    		case 5:								//5号消息，刷新View显示新内容
    			if(gv!=null){
    				gv.invalidate();
    			}
    			break;    		
    		case 100:							//100号消息，用来销毁标志bulletFlag为false的子弹
    			synchronized(gv.myBullets){		//线程同步，防止多个线程同时去操作这个链表
	    			for(i=0;i<gv.myBullets.size();i++){
	    				Bullet one=gv.myBullets.get(i);
	    				if(one!=null && one.bulletFlag==false){	//标志为false的子弹，就是要销毁的子弹
	    					gv.myBullets.remove(i);		//将该子弹从链表中删除
	    					one=null;					//将该子弹赋值为null，那么Android就会收回这个空间
	    				}
	    			}
    			}
    			break;
    		case 101:							//101消息，有一个敌人坦克被消灭，需要处理
    			int k;
    			synchronized(gv.enemyTanks){
    				for(k=0;k<gv.enemyTanks.size();k++){
    					OneTank one=gv.enemyTanks.get(k);
    					if(one!=null && one.enemyFlag==false){	//如果one这个敌人被消灭了
    						if(gv.hasABonus==false){					//如果当前没有Bonus，则按20%概率生成一个Bonus
    							int newabonusprobability=(int)(Math.random()*100);
    							if(newabonusprobability<20){			//0-19：生成一个Bonus
    								int bonusType=(int)(Math.random()*8)+1;	//bonusType得到1~8的整数
    								gv.oneBonus=new Bonus(gv,bonusType,one.tankLine,one.tankRow);
    								gv.hasABonus=true;
    							}
    						}
    						gv.enemyTanks.remove(k);
    						one=null;
    						gv.leftEnemyTanks--;			//敌人坦克数减1
    						if(gv.leftEnemyTanks<=0){		//如果所有的敌人坦克都被消灭，则过一关，发送301号消息
    							sendEmptyMessage(301);
    						}else if(gv.leftEnemyTanks>=6){		//如果敌人还有坦克，那么就再生成一个新的坦克
    							int tankLine,tankRow=0;
    							tankLine=0;
    							switch(k%3){	//根据坦克出生的位置编号来确定其行和列
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
    							one=new OneTank(gv,k%2+1,tankLine,tankRow,k%3,2,1,(k%2+1)==1?1000:500,sleepSpan);
    							gv.enemyTanks.add(one);					//将这个坦克插入链表中
    							Log.d("Add", "增加一个新坦克成功");
    						}    						
    					}
    				}
    			}
    			break;
    		case 200:		//200号消息：销毁一个敌人的子弹
    			int m;
    			synchronized(gv.enemyBullets){		//线程同步，防止多个线程同时去操作这个链表
	    			for(m=0;m<gv.enemyBullets.size();m++){
	    				Bullet one=gv.enemyBullets.get(m);
	    				if(one!=null && one.bulletFlag==false){	//标志为false的子弹，就是要销毁的子弹
	    					gv.enemyBullets.remove(m);		//将该子弹从链表中删除
	    					one=null;					//将该子弹赋值为null，那么Android就会收回这个空间
	    				}
	    			}
	    			Log.d("敌人子弹剩余量", ""+gv.enemyBullets.size());
    			}
    			break;
    		case 201:		//201消息，我方坦克被消灭了一个，需要收回该坦克资源及所有的子弹，并再new出一个新的坦克
    			gv.myTank.myFlag=false;		//停止坦克运动线程
    			gv.nMyTanks--;
    			if(gv.nMyTanks>=0)				//如果我方还剩下坦克，则
    				gv.initMyTank();			//调用方法，重新new一个我方坦克
    			else{
    				sendEmptyMessage(300);		//向自己发送300号消息：GameOver了
    			}
    			Bullet one;
    			int n;
    			for(n=0;n<gv.myBullets.size();n++){	//收回所有的子弹
    				one=gv.myBullets.get(n);
    				one.bulletFlag=false;
    				gv.myBullets.remove(n);
    				one=null;
    			}
    			break;
    		case 300:							//300号消息，处理GameOver
    			gv.hasGameOver=true;
    			gv.myTank.myFlag=false;			//我方坦克无法移动
    			Log.d("GameOver", "GameOver");
    			break;
    		case 301:							//301号消息：过了一关了
    			level=(level+1)%TankMaps.maxLevels;		//过一关，但是如果已经是最后一关，则又回到第0关
    			int nMyTanks=gv.nMyTanks;		//记住目前我方还剩下多少个坦克
    			gv.gameViewFlag=false;			//先让刷新屏幕的线程停下来
    			for(i=0;i<gv.myBullets.size();i++){	//让所有的我方子弹都销毁掉
    				one=gv.myBullets.get(i);
    				one.bulletFlag=false;
    				gv.myBullets.remove(i);
    			}
    			for(i=0;i<gv.enemyBullets.size();i++){	//让所有的敌人子弹都销毁掉
    				one=gv.enemyBullets.get(i);
    				one.bulletFlag=false;
    				gv.enemyBullets.remove(i);
    			}
    			gv.myTank.myFlag=gv.myTank.enemyFireFlag=gv.myTank.enemyFlag=false;	//销毁我方坦克
    			for(i=0;i<gv.enemyTanks.size();i++){			//销毁所有的敌人的坦克
    				OneTank oneT=gv.enemyTanks.get(i);
    				oneT.myFlag=oneT.enemyFireFlag=oneT.enemyFlag=false;
    				gv.enemyTanks.remove(i);
    			}    			
    			gv=new GameView(TankActivity.this);
    			gv.nMyTanks=nMyTanks;				//还原我方坦克数
    			gv.nMyTanks++;						//每过一关，奖励我方一个坦克
    			setContentView(gv);
    			break;
    		case 400:								//收回当前的Bonus
    			gv.oneBonus.bonusSwitchStatusFlag=false;
    			gv.hasABonus=false;					//表明当前已经没有了Bonus，从而可以再产生一个Bonus
    			break;
    		}    		
    	};
    };
}