import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Bean: Each bean is assigned a skill level from 0-9 on creation according to a
 * normal distribution with average SKILL_AVERAGE and standard deviation
 * SKILL_STDEV. The formula to calculate the skill level is:
 * 
 * <p>SKILL_AVERAGE = (double) (SLOT_COUNT-1) * 0.5
 * SKILL_STDEV = (double) Math.sqrt(SLOT_COUNT * 0.5 * (1 - 0.5))
 * SKILL_LEVEL = (int) Math.round(rand.nextGaussian() * SKILL_STDEV + SKILL_AVERAGE)
 * 
 * <p>A skill level of 9 means it always makes the "right" choices (pun intended)
 * when the machine is operating in skill mode ("skill" passed on command line).
 * That means the bean will always go right when a peg is encountered, resulting
 * it falling into slot 9. A skill evel of 0 means that the bean will always go
 * left, resulting it falling into slot 0. For the in-between skill levels, the
 * bean will first go right then left. For example, for a skill level of 7, the
 * bean will go right 7 times then go left twice.
 * 
 * <p>Skill levels are irrelevant when the machine operates in luck mode. In that
 * case, the bean will have a 50/50 chance of going right or left, regardless of
 * skill level. The formula to calculate the direction is: rand.nextInt(2). If
 * the return value is 0, the bean goes left. If the return value is 1, the bean
 * goes right.
 */

public class BeanImpl implements Bean {
	
	private int xpos;
	private int skillLevel;
	private int skillRemaining;
	private double skillAverage;
	private double skillStdDev;
	private boolean isLuck;
	private Random rng;

	/**
	 * Constructor - creates a bean in either luck mode or skill mode.
	 * 
	 * @param slotCount	the number of slots in the machine
	 * @param isLuck	whether the bean is in luck mode
	 * @param rand		the random number generator
	 */
	BeanImpl(int slotCount, boolean isLuck, Random rand) {
		this.isLuck = isLuck;
 		this.rng = rand;
 		this.xpos = 0;
		this.skillAverage = (double) (slotCount - 1) * 0.5;
 		this.skillStdDev = (double) Math.sqrt(slotCount * 0.5 * (1 - 0.5));
 		this.skillLevel = (int) Math.round(rand.nextGaussian() * this.skillStdDev + this.skillAverage);
 		this.skillLevel = (this.skillLevel > (slotCount - 1)) ? (slotCount - 1) : this.skillLevel;
 		this.skillLevel = (this.skillLevel < 0) ? 0 : this.skillLevel;
 		this.skillRemaining = this.skillLevel;
	}
	
	/**
	 * Returns the current X-coordinate position of the bean in the logical coordinate system.
	 * 
	 * @return the current X-coordinate of the bean
	 */
	public int getXPos() {
		return this.xpos;
	}

	/**
	 * Resets the bean to its initial state. The X-coordinate should be initialized
	 * to 0. 
	 */
	public void reset() {
		this.skillRemaining = this.skillLevel;
		this.xpos = 0;
	}
	
	/**
	 * Chooses left or right randomly (if luck) or according to skill. If the return
	 * value of rand.nextInt(2) is 0, the bean goes left. Otherwise, the bean goes
	 * right.  The X-coordinate is updated accordingly.
	 */
	public void choose() {
		if (this.isLuck) {
			int leftOrRight = this.rng.nextInt(2);
			if (leftOrRight == 1) {
				this.xpos++;
			}
		} else if (this.skillRemaining > 0) {
			this.xpos++;
			this.skillLevel--;
		}
	}
}