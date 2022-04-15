import java.util.Formatter;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Random;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>BeanCounterLogic: The bean counter, also known as a quincunx or the Galton
 * box, is a device for statistics experiments named after English scientist Sir
 * Francis Galton. It consists of an upright board with evenly spaced nails (or
 * pegs) in a triangular form. Each bean takes a random path and falls into a
 * slot.
 *
 * <p>Beans are dropped from the opening of the board. Every time a bean hits a
 * nail, it has a 50% chance of falling to the left or to the right. The piles
 * of beans are accumulated in the slots at the bottom of the board.
 * 
 * <p>This class implements the core logic of the machine. The MainPanel uses the
 * state inside BeanCounterLogic to display on the screen.
 * 
 * <p>Note that BeanCounterLogic uses a logical coordinate system to store the
 * positions of in-flight beans.For example, for a 4-slot machine:
 *                      (0, 0)
 *               (0, 1)        (1, 1)
 *        (0, 2)        (1, 2)        (2, 2)
 *  (0, 3)       (1, 3)        (2, 3)       (3, 3)
 * [Slot0]       [Slot1]       [Slot2]      [Slot3]
 */

public class BeanCounterLogicImpl implements BeanCounterLogic {
	// Member variables & data structures
	private int totalSlots;
	private int totalBeans;
	private Bean[] beansOnBoard;
	private Stack<Bean> remainingBeans;
	private LinkedList<Bean>[] beansInSlots;

	/**
	 * Constructor - creates the bean counter logic object that implements the core
	 * logic with the provided number of slots.
	 * 
	 * @param slotCount the number of slots in the machine
	 */
	BeanCounterLogicImpl(int slotCount) {
		assert slotCount >= 0;
		this.totalSlots = slotCount;
		this.beansOnBoard = new Bean[slotCount];
		this.remainingBeans = new Stack<Bean>();
		this.beansInSlots = new LinkedList[slotCount];
		for (int i = 0; i < slotCount; i++) {
			this.beansInSlots[i] = new LinkedList<Bean>();
		}
	}

	/**
	 * Returns the number of slots the machine was initialized with.
	 * 
	 * @return number of slots
	 */
	public int getSlotCount() {
		return totalSlots;
	}
	
	/**
	 * Returns the number of beans remaining that are waiting to get inserted.
	 * 
	 * @return number of beans remaining
	 */
	public int getRemainingBeanCount() {
		return remainingBeans.size();
	}

	/**
	 * Returns the x-coordinate for the in-flight bean at the provided y-coordinate.
	 * 
	 * @param yPos the y-coordinate in which to look for the in-flight bean
	 * @return the x-coordinate of the in-flight bean; if no bean in y-coordinate, return NO_BEAN_IN_YPOS
	 */
	public int getInFlightBeanXPos(int yPos) {
		int xpos = NO_BEAN_IN_YPOS;
		if (yPos >= 0 && yPos < getSlotCount() && beansOnBoard[yPos] != null) {
			xpos = beansOnBoard[yPos].getXPos();
		}
		return xpos;
	}

	/**
	 * Returns the number of beans in the ith slot.
	 * 
	 * @param i index of slot
	 * @return number of beans in slot
	 */
	public int getSlotBeanCount(int i) {
		int beansInIthSlot = 0;
		if (i >= 0 && i < getSlotCount()) {
			beansInIthSlot = beansInSlots[i].size();
		}
		return beansInIthSlot;
	}

	/**
	 * Calculates the average slot number of all the beans in slots.
	 * 
	 * @return Average slot number of all the beans in slots.
	 */
	public double getAverageSlotBeanCount() {
		double avg = 0.0;
		double runningTotal = 0.0;
		int totalBeansInSlots = 0;
		for (int i = 0; i < getSlotCount(); i++) {
			runningTotal += (getSlotBeanCount(i) * i);
			totalBeansInSlots += getSlotBeanCount(i);
		}
		avg = (totalBeansInSlots > 0) ? (runningTotal / totalBeansInSlots) : 0.0;
		return avg;
	}

	/**
	 * Removes the lower half of all beans currently in slots, keeping only the
	 * upper half. If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void upperHalf() {
		int totalBeansInSlots = 0;
		for (int i = 0; i < getSlotCount(); i++) {
			totalBeansInSlots += getSlotBeanCount(i);
		}
		int beansToRemove = ((totalBeansInSlots % 2) == 0) ? (totalBeansInSlots / 2) : ((totalBeansInSlots - 1) / 2);
		for (int i = 0; i < getSlotCount(); i++) {
			while ((!beansInSlots[i].isEmpty()) && (beansToRemove > 0)) {
				beansInSlots[i].removeLast();
				beansToRemove--;
			}
			if (beansToRemove == 0) {
				break;
			}
		}
	}

	/**
	 * Removes the upper half of all beans currently in slots, keeping only the
	 * lower half.  If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void lowerHalf() {
		int totalBeansInSlots = 0;
		for (int i = 0; i < getSlotCount(); i++) {
			totalBeansInSlots += getSlotBeanCount(i);
		}
		int beansToRemove = ((totalBeansInSlots % 2) == 0) ? (totalBeansInSlots / 2) : ((totalBeansInSlots - 1) / 2);
		for (int i = (getSlotCount() - 1); i >= 0; i--) {
			while ((!beansInSlots[i].isEmpty()) && (beansToRemove > 0)) {
				beansInSlots[i].removeLast();
				beansToRemove--;
			}
			if (beansToRemove == 0) {
				break;
			}
		}
	}

	/**
	 * A hard reset. Initializes the machine with the passed beans. The machine
	 * starts with one bean at the top.
	 * 
	 * @param beans array of beans to add to the machine
	 */
	public void reset(Bean[] beans) {
		beansOnBoard = new Bean[getSlotCount()];
		remainingBeans = new Stack<Bean>();
		beansInSlots = new LinkedList[getSlotCount()];
		for (int i = 0; i < getSlotCount(); i++) {
			beansInSlots[i] = new LinkedList<Bean>();
		}
		for (int i = 0; i < beans.length; i++) {
			remainingBeans.push(beans[i]);
		}	
		if (!remainingBeans.isEmpty()) {
			beansOnBoard[0] = remainingBeans.pop();
		}
	}

	/**
	 * Repeats the experiment by scooping up all beans in the slots and all beans
	 * in-flight and adding them into the pool of remaining beans. As in the
	 * beginning, the machine starts with one bean at the top.
	 */
	public void repeat() {
		for (int i = 0; i < getSlotCount(); i++) {
			while (!beansInSlots[i].isEmpty()) {
				remainingBeans.push(beansInSlots[i].removeLast());
			}
			if (beansOnBoard[i] != null) {
				remainingBeans.push(beansOnBoard[i]);
				beansOnBoard[i] = null;
			}
		}
		if (!remainingBeans.isEmpty()) {
			beansOnBoard[0] = remainingBeans.pop();
		}
	}

	/**
	 * Advances the machine one step. All the in-flight beans fall down one step to
	 * the next peg. A new bean is inserted into the top of the machine if there are
	 * beans remaining.
	 * 
	 * @return whether there has been any status change. If there is no change, that
	 *         means the machine is finished.
	 */
	public boolean advanceStep() {
		boolean statusChange = false;
		for (int i = (getSlotCount() - 1); i >= 0; i--) {
			Bean b = beansOnBoard[i];
			if (b != null) {
				if (i == (getSlotCount() - 1)) {
					if (b.getXPos() >= 0 && b.getXPos() < getSlotCount()) {
						beansInSlots[b.getXPos()].add(b);
						beansOnBoard[i] = null;
					}
				} else {
					b.choose();
					beansOnBoard[i + 1] = b;
					beansOnBoard[i] = null;
				}
				statusChange = true;
			} else if ((b == null) && (i < (getSlotCount() - 1))) {
				beansOnBoard[i + 1] = null;
			}
		}
		if (!remainingBeans.isEmpty()) {
			beansOnBoard[0] = remainingBeans.pop();
		}
		return statusChange;
	}
	
	/**
	 * Number of spaces in between numbers when printing out the state of the machine.
	 * Make sure the number is odd (even numbers don't work as well).
	 */
	private int xspacing = 3;

	/**
	 * Calculates the number of spaces to indent for the given row of pegs.
	 * 
	 * @param yPos the y-position (or row number) of the pegs
	 * @return the number of spaces to indent
	 */
	private int getIndent(int yPos) {
		int rootIndent = (getSlotCount() - 1) * (xspacing + 1) / 2 + (xspacing + 1);
		return rootIndent - (xspacing + 1) / 2 * yPos;
	}

	/**
	 * Constructs a string representation of the bean count of all the slots.
	 * 
	 * @return a string with bean counts for each slot
	 */
	public String getSlotString() {
		StringBuilder bld = new StringBuilder();
		Formatter fmt = new Formatter(bld);
		String format = "%" + (xspacing + 1) + "d";
		for (int i = 0; i < getSlotCount(); i++) {
			fmt.format(format, getSlotBeanCount(i));
		}
		fmt.close();
		return bld.toString();
	}

	/**
	 * Constructs a string representation of the entire machine. If a peg has a bean
	 * above it, it is represented as a "1", otherwise it is represented as a "0".
	 * At the very bottom is attached the slots with the bean counts.
	 * 
	 * @return the string representation of the machine
	 */
	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
		    value="VA_FORMAT_STRING_USES_NEWLINE", 
		    justification="I know we should be using %n instead of \n, but JPF for some reason does not like %n")
	public String toString() {
		StringBuilder bld = new StringBuilder();
		Formatter fmt = new Formatter(bld);
		for (int yPos = 0; yPos < getSlotCount(); yPos++) {
			int xBeanPos = getInFlightBeanXPos(yPos);
			for (int xPos = 0; xPos <= yPos; xPos++) {
				int spacing = (xPos == 0) ? getIndent(yPos) : (xspacing + 1);
				String format = "%" + spacing + "d";
				if (xPos == xBeanPos) {
					fmt.format(format, 1);
				} else {
					fmt.format(format, 0);
				}
			}
			fmt.format("\n");
		}
		fmt.close();
		return bld.toString() + getSlotString();
	}

	/**
	 * Prints usage information.
	 */
	public static void showUsage() {
		System.out.println("Usage: java BeanCounterLogic slot_count bean_count <luck | skill> [debug]");
		System.out.println("Example: java BeanCounterLogic 10 400 luck");
		System.out.println("Example: java BeanCounterLogic 20 1000 skill debug");
	}
	
	/**
	 * Auxiliary main method. Runs the machine in text mode with no bells and
	 * whistles. It simply shows the slot bean count at the end.
	 * 
	 * @param args commandline arguments; see showUsage() for detailed information
	 */
	public static void main(String[] args) {
		boolean debug;
		boolean luck;
		int slotCount = 0;
		int beanCount = 0;
		//this.totalBeans = beanCount;

		if (args.length != 3 && args.length != 4) {
			showUsage();
			return;
		}

		try {
			slotCount = Integer.parseInt(args[0]);
			beanCount = Integer.parseInt(args[1]);
		} catch (NumberFormatException ne) {
			showUsage();
			return;
		}
		if (beanCount < 0) {
			showUsage();
			return;
		}

		if (args[2].equals("luck")) {
			luck = true;
		} else if (args[2].equals("skill")) {
			luck = false;
		} else {
			showUsage();
			return;
		}
		
		if (args.length == 4 && args[3].equals("debug")) {
			debug = true;
		} else {
			debug = false;
		}

		// Create the internal logic
		BeanCounterLogicImpl logic = new BeanCounterLogicImpl(slotCount);
		// Create the beans (in luck mode)
		BeanImpl[] beans = new BeanImpl[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = new BeanImpl(slotCount, luck, new Random());
		}
		// Initialize the logic with the beans
		logic.reset(beans);

		if (debug) {
			System.out.println(logic.toString());
		}

		// Perform the experiment
		while (true) {
			if (!logic.advanceStep()) {
				break;
			}
			if (debug) {
				System.out.println(logic.toString());
			}
		}
		// display experimental results
		System.out.println("Slot bean counts:");
		System.out.println(logic.getSlotString());
	}
}
