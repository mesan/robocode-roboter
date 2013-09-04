package G15;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
//import robocode.util.*;

public class CakeBot extends AdvancedRobot {
	String targetName;
	boolean backwards = false;
	double gunTurnAmt;
	double firePower;
	int count = 0;
	int patience = 0;
	int radiusThreshold;
	boolean blink = true;

	public void run() {

		targetName = null;
		setAdjustGunForRobotTurn(true);
		gunTurnAmt = 10;

		radiusThreshold = 120;

		while (true) {
			setTurnGunRight(gunTurnAmt);
			count++;

			if (count > 7) {
				targetName = null;
			} else if (count > 4) {
				gunTurnAmt = 10;
			} else if (count > 2) {
				gunTurnAmt = -10;
			}

			if ((getHeading() == 0 && getY() > getBattleFieldHeight()
					- radiusThreshold)
					|| (getHeading() == 90 && getX() > getBattleFieldWidth()
							- radiusThreshold)
					|| (getHeading() == 180 && getY() < radiusThreshold)
					|| (getHeading() == 270 && getX() < radiusThreshold)) {
				setTurnRight(45);
			}

			if (backwards == true) {
				setBack(50);
			} else {
				setAhead(50);
			}
			setMaxVelocity(Math.random() *3 + 5);
			
			if (blink) {
				setColors(Color.green, Color.red, Color.black);
				setBulletColor(Color.pink);
				setScanColor(Color.pink);
				blink = false;
			} else {
				setColors(Color.yellow, Color.blue, Color.white);
				setBulletColor(Color.pink);
				setScanColor(Color.red);
				blink = true;
			}
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (targetName != null && !e.getName().equals(targetName)) {
			return;
		}

		if (targetName == null) {
			targetName = e.getName();
			System.out.print("Target: " + targetName + "\n");
		}
		count = 0;

		if (e.getDistance() > 400 && getOthers() > 1 && patience < 10) {
			patience++;
			System.out.print("Target of out of range!" + "\n");
			targetName = null;
			return;
		}
		
		firePower = Math.min(500/e.getDistance(), 3);
		
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getGunHeading()));
		System.out.println(gunTurnAmt);
		setTurnGunRight(gunTurnAmt);
		setFire(firePower);
		patience = 0;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		setTurnRight(e.getBearing() + 90);
	}

	public void onHitRobot(HitRobotEvent e) {
		targetName = e.getName();
		gunTurnAmt = normalRelativeAngleDegrees(e.getBearing()
				+ (getHeading() - getRadarHeading()));
		setTurnGunRight(gunTurnAmt);
		setFire(firePower);

		if (-90.0 < e.getBearing() && e.getBearing() < 90.0) {
			backwards = true;
		} else {
			backwards = false;
		}
	}

	public void onHitWall(HitWallEvent e) {
		backwards = false;
		setTurnRight(e.getBearing() + 90);
	}
	
}