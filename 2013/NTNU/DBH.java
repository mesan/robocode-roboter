package uji;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import uji.Follow.Vector;

public class DBH extends AdvancedRobot
{
	static ArrayList<Enemy>enemies=new ArrayList<Enemy>();
	double shootx=0,shooty=0;
	boolean log=false;
	int hit=0;
	int missed=0;
	int method=1;
	Vector movedir;
	double moveposx=0;
	double moveposy=0;
	public void run()
	{
		setTurnRadarRightRadians(Double.MAX_VALUE);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setColors(new Color(19, 136, 8),new Color(0, 168, 10),new Color(128, 70, 27));
		hit=0;
		missed=0;
		method=1;
		movedir=new Vector(0,0,0,0);
		moveposx=getX();
		moveposy=getY();
		
		while(true)
		{
			int closest=0;
			double closestdistance=Double.MAX_VALUE;
			for(int i=0;i<enemies.size();i++)
			{
				if(new Vector(getX(),getY(),enemies.get(i).x,enemies.get(i).y).getLength()<closestdistance)
				{
					closest=i;
					closestdistance=new Vector(getX(),getY(),enemies.get(i).x,enemies.get(i).y).getLength();
				}
				enemies.get(i).update();
			}
			if(getOthers()==1)
			{
				log=true;
				if((double)hit/(double)missed<0.2 && hit+missed>=10)
				{
					method=0;
				}
			}
			if(enemies.size()>0)
			{
				if(method==1)
					linearPredict(enemies.get(closest));
				else
					headOn(enemies.get(closest));
				if(getEnergy()>4)
					shootAt(shootx,shooty,2);
			}
			double sumx=0,sumy=0;
			double maxforce=0;
			int maxint=0;
			for(int i=0;i<enemies.size();i++)
			{
				enemies.get(i).update();
				sumx+=enemies.get(i).speed.x;
				sumy+=enemies.get(i).speed.y;
				double a=new Vector(getX(),getY(),enemies.get(i).x,enemies.get(i).y).getAngle(new Vector(0,0,0,1));
				double force=2000/new Vector(getX(),getY(),enemies.get(i).x,enemies.get(i).y).getLength();
				sumx-=Math.sin(a)*force;
				sumy-=Math.cos(a)*force;
				if(force>maxforce)
				{
					maxforce=force;
					maxint=i;
				}
			}
			sumx-=800/(800-getX());
			sumx+=800/getX();
			sumy-=600/(600-getY());
			sumy+=600/getY();
			movedir=new Vector(0,0,sumx,sumy);
			moveposx=getX()+sumx;
			moveposy=getY()+sumy;
			setGoto(moveposx,moveposy);
			System.out.println((double)hit/(double)missed);
			execute();
		}
	}
	boolean setGoto(double x,double y)
	{
		Vector direction=new Vector(0,0,Math.sin(getHeadingRadians()),Math.cos(getHeadingRadians()));
		Vector toPoint=new Vector(getX(),getY(),x,y);
		if(direction.getCross(toPoint)<0)
		{
			if(direction.getDot(toPoint)<0)
				setTurnLeftRadians(Math.PI-direction.getAngle(toPoint));
			else
				setTurnRightRadians(direction.getAngle(toPoint));
		}
		else
		{
			if(direction.getDot(toPoint)<0)
				setTurnRightRadians(Math.PI-direction.getAngle(toPoint));
			else
				setTurnLeftRadians(direction.getAngle(toPoint));
		}
		if(direction.getDot(toPoint)<0)
			setBack(getDistance(getX(),getY(),x,y));
		else
			setAhead(getDistance(getX(),getY(),x,y));
		if(getDistance(getX(),getY(),x,y)<=1)
			return true;
		return false;
	}
	double getDistance(double x1,double y1,double x2,double y2)
	{
		return Math.sqrt(((x2-x1)*(x2-x1))+((y1-y2)*(y1-y2)));
	}
	private void linearPredict(Enemy enemy)
	{
		double bulletSpeed=20-(3*2);
		double startx=getX(),starty=getY();
		double traveltime=new Vector(startx,starty,enemy.x,enemy.y).getLength()/bulletSpeed;
		double predx,predy;
		predx=enemy.x+(enemy.speed.x*traveltime);
		predy=enemy.y+(enemy.speed.y*traveltime);
		while(Math.abs(new Vector(startx,starty,predx,predy).getLength()-(bulletSpeed*traveltime))>3)
		{
			traveltime=new Vector(getX(),getY(),predx,predy).getLength()/bulletSpeed;
			predx=enemy.x+(enemy.speed.x*traveltime);
			predy=enemy.y+(enemy.speed.y*traveltime);
		}
		if(predx>getBattleFieldWidth()-18)
			predx=getBattleFieldWidth()-18;
		else if(predx<18)
			predx=18;
		if(predy>getBattleFieldHeight()-18)
			predy=getBattleFieldHeight()-18;
		else if(predy<18)
			predy=18;
		shootx=predx;
		shooty=predy;
	}
	private void headOn(Enemy enemy)
	{
		shootx=enemy.x;
		shooty=enemy.y;
	}
	public void onScannedRobot(ScannedRobotEvent e)
	{
		boolean found=false;
		for(int i=0;i<enemies.size();i++)
		{
			if(enemies.get(i).name.equals(e.getName()))
			{
				enemies.get(i).angle=e.getHeadingRadians();
				enemies.get(i).speed=new Vector(0,0,Math.sin(enemies.get(i).angle)*e.getVelocity(),Math.cos(enemies.get(i).angle)*e.getVelocity());
				enemies.get(i).x=getX()+Math.sin(Utils.normalAbsoluteAngle(e.getBearingRadians()+getHeadingRadians()))*e.getDistance();
				enemies.get(i).y=getY()+Math.cos(Utils.normalAbsoluteAngle(e.getBearingRadians()+getHeadingRadians()))*e.getDistance();
				enemies.get(enemies.size()-1).lateralVelocity=e.getVelocity()
						*Math.sin(e.getHeadingRadians()-e.getBearingRadians()
								+getHeadingRadians());
				found=true;
			}
		}
		if(!found)
		{
			enemies.add(new Enemy(e.getName(),getX()+Math.sin(Utils.normalAbsoluteAngle(e.getBearingRadians()+getHeadingRadians()))*e.getDistance(),
					getX()+Math.cos(Utils.normalAbsoluteAngle(e.getBearingRadians()+getHeadingRadians()))*e.getDistance(),
					e.getHeadingRadians(),
					e.getVelocity()));
			enemies.get(enemies.size()-1).lateralVelocity=e.getVelocity()
					*Math.sin(e.getHeadingRadians()-e.getBearingRadians()
							+getHeadingRadians());
		}
	}
	public void onPaint(Graphics2D g)
	{
		g.setColor(Color.red);
		for(int i=0;i<enemies.size();i++)
		{
			g.drawOval((int)enemies.get(i).x-20,(int)enemies.get(i).y-20,40,40);
		}
		g.setColor(Color.GREEN);
		g.drawOval((int)shootx,(int)shooty,10,10);
	}
	boolean shootAt(double x,double y,double power)
	{
		Vector a=new Vector(getX(),getY(),x,y);
		setTurnGunLeftRadians(Utils.normalRelativeAngle(getGunHeadingRadians()-a.getAngle(new Vector(0,0,0,1))));
		if(Math.abs(getGunHeadingRadians()-a.getAngle(new Vector(0,0,0,1)))<0.1)
			fire(power);
		return false;
	}
	class Enemy
	{
		double x;
		double y;
		double angle;
		double lateralVelocity;
		Vector speed;
		String name;
		Enemy(String inname,double inx,double iny,double inangle,double velocity)
		{
			name=inname;
			x=inx;
			y=iny;
			angle=inangle;
			speed=new Vector(0,0,Math.sin(angle)*velocity,Math.cos(angle)*velocity);
		}
		void update()
		{
			x+=speed.x;
			y+=speed.y;
		}
	}
	class Vector
	{
		double x,y;
		Vector(double x1,double y1,double x2,double y2)
		{
			x=x2-x1;
			y=y2-y1;
		}
		double getLength()
		{
			return Math.sqrt((x*x)+(y*y));
		}
		double getAngle(Vector a)
		{
			double angle=Math.acos(this.getDot(a)/(this.getLength()*a.getLength()));
			if(this.getCross(a)<0)
				return (Math.PI*2)-angle;
			else
				return angle;
		}
		double getDot(Vector a)
		{
			return (x*a.x)+(y*a.y);
		}
		double getCross(Vector a)
		{
			return (x*a.y)-(y*a.x);
		}
	}
	public void onBulletHit(BulletHitEvent e)
	{
		if(log)
			hit++;
	}
	public void onBulletMissed(BulletMissedEvent e)
	{
		if(log)
			missed++;
	}
	public void onRobotDeath(RobotDeathEvent e)
	{
		for(int i=0;i<enemies.size();i++)
		{
			if(enemies.get(i).name.equals(e.getName()))
			{
				enemies.remove(i);
				System.out.println(enemies.size());
				break;
			}
		}
	}
}
