import java.util.Formatter;
import java.util.Random;
import java.util.LinkedList;

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
	private int totalSlots; // total slots indicated by user upon initializing a BeanCounterLogic object
	private int xspacing; // var for printing out state of machine
	private int totalBeans; // total number of beans being poured into Galton board
	private Bean[] movingBeans; // beans moving on Galton Board
	private LinkedList<Bean> remainingBeans; // beans that have not yet went down the Galton Board
	private LinkedList<Bean>[] slots; // array of linked lists of Beans, where each LinkedList in this array represents the corresponding slot

	/**
	 * Constructor - creates the bean counter logic object that implements the core
	 * logic with the provided number of slots.
	 * 
	 * @param slotCount the number of slots in the machine
	 */
	BeanCounterLogicImpl(int slotCount) {
		// spacing between
		this.xspacing = 3;

		// Total Slots on the Galton Board
		this.totalSlots = slotCount;

		// Array of Beans falling down Galton Board
		this.movingBeans = new Bean[slotCount];

		// Linked List of Beans that have not yet went down Galton Board
		this.remainingBeans = new LinkedList<>();

		// array of linked lists of Beans
		this.slots = new LinkedList[slotCount];
		
		// each entry in the array contains a list of Beans in the respective slot
		for(int i=0; i < totalSlots; i++){
			this.slots[i] = new LinkedList<>();
		}
	}

	/**
	 * Returns the number of slots the machine was initialized with.
	 * 
	 * @return number of slots
	 */
	public int getSlotCount() {
		// when the BeanCounterLogicImpl object was initialized it was passed a number of slots, which I saved to this member variable
		return this.totalSlots;
	}
	
	/**
	 * Returns the number of beans remaining that are waiting to get inserted.
	 * 
	 * @return number of beans remaining
	 */
	public int getRemainingBeanCount() {
		// return number of beans in LinkedList of beans not yet in slots or on the Galton Board
		return this.remainingBeans.size();
	}

	/**
	 * Returns the x-coordinate for the in-flight bean at the provided y-coordinate.
	 * 
	 * @param yPos the y-coordinate in which to look for the in-flight bean
	 * @return the x-coordinate of the in-flight bean; if no bean in y-coordinate, return NO_BEAN_IN_YPOS
	 */
	public int getInFlightBeanXPos(int yPos) {
		if(this.movingBeans[yPos] != null){
			// if a bean is on the Galton Board at the indicated yPos, call get the beans x position
			// since only one bean can be at a given y position, we only need an array the size of the number of slots to keep track of which beans are on the Galton board
			return this.movingBeans[yPos].getXPos();
		} else {
			// otherwise there was no bean on the Galton Board at yPos
			return NO_BEAN_IN_YPOS;
		}	
	}

	/**
	 * Returns the number of beans in the ith slot.
	 * 
	 * @param i index of slot
	 * @return number of beans in slot
	 */
	public int getSlotBeanCount(int i) {
		// get the size of the LinkedList at slot i if i is within the range of possible slots
		if(i >= 0 && i < getSlotCount()){
			return this.slots[i].size();
		} else {
			// otherwise return 0 since the slot doesn't exist (e.g. there are no beans in a non-existing slot)
			return 0;
		}
	}

	/**
	 * Calculates the average slot number of all the beans in slots.
	 * 
	 * @return Average slot number of all the beans in slots.
	 */
	public double getAverageSlotBeanCount() {
		int totalInSlots = 0;
		double averageSlotNum = 0.0;
		for(int i=0; i < this.getSlotCount(); i++){
			totalInSlots += this.getSlotBeanCount(i);
			averageSlotNum += (this.getSlotBeanCount(i) * i);
		}
		if(totalInSlots > 0){
			averageSlotNum /= totalInSlots;
		}
		return averageSlotNum;
	}

	/**
	 * Removes the lower half of all beans currently in slots, keeping only the
	 * upper half. If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void upperHalf() {
		// First, determine the total number of beans in the slots
		int totalBeansInSlots = 0;

		// iterate through the slots, adding the total beans in each slot to the running total
		for(int i=0; i < getSlotCount(); i++){
			totalBeansInSlots += getSlotBeanCount(i);
		}

		// now that we have the total number of beans in the slots we need to determine how many beans to remove
		// if there is an even number of beans in the slots then just remove half, otherwise remove (n-1)/2 beans
		int beansToRemove = (totalBeansInSlots % 2) == 0 ? (totalBeansInSlots/2) : ((totalBeansInSlots-1)/2);

		// start removing beans from slots starting at the lower end of the slots while there are still beans to remove
		for(int i=0; i < getSlotCount(); i++){
			// while there are beans still in the current linkedlist and there are still beans to remove
			while((!this.slots[i].isEmpty()) && beansToRemove > 0){
				this.slots[i].pop(); // remove a bean from the slot
				beansToRemove--; // one less bean to remove
			}
			if(beansToRemove == 0)
				break;
		}
	}

	/**
	 * Removes the upper half of all beans currently in slots, keeping only the
	 * lower half.  If there are an odd number of beans, remove (N-1)/2 beans, where
	 * N is the number of beans. So, if there are 3 beans, 1 will be removed and 2
	 * will be remaining.
	 */
	public void lowerHalf() {
		// First, determine the total number of beans in the slots
		int totalBeansInSlots = 0;

		// iterate through the slots, adding the total beans in each slot to the running total
		for(int i=0; i < this.slots.length; i++){
			totalBeansInSlots += getSlotBeanCount(i);
		}

		// now that we have the total number of beans in the slots we need to determine how many beans to remove
		// if there is an even number of beans in the slots then just remove half, otherwise remove (n-1)/2 beans
		int beansToRemove = (totalBeansInSlots % 2) == 0 ? (totalBeansInSlots/2) : ((totalBeansInSlots-1)/2);

		// start removing beans from slots starting at the upper end of the slots while there are still beans to remove
		for(int i = getSlotCount() - 1; i >= 0; i--){
			// while there are beans still in the current linkedlist and there are still beans to remove
			while((!this.slots[i].isEmpty()) && beansToRemove > 0){
				this.slots[i].pop();
				beansToRemove--;
			}
			if(beansToRemove == 0)
				break;
		}
	}

	/**
	 * A hard reset. Initializes the machine with the passed beans. The machine
	 * starts with one bean at the top.
	 * 
	 * @param beans array of beans to add to the machine
	 */
	public void reset(Bean[] beans) {
		// first remove all beans from the slots
		// and remove all beans on the Galton Board
		for(int i=0; i < getSlotCount(); i++){
			this.slots[i].clear();
			this.movingBeans[i] = null;
		}

		// clear all remaining beans
		this.remainingBeans.clear();

		// add all beans to remaining beans now that everything is cleared
		for(int i=0; i < beans.length; i++){
			this.remainingBeans.add(beans[i]);
		}

		// remove a bean from remainingBeans if there are any remaining and add to top of Galton board
		if(this.remainingBeans.size() > 0)
			this.movingBeans[0] = this.remainingBeans.remove();
	}

	/**
	 * Repeats the experiment by scooping up all beans in the slots and all beans
	 * in-flight and adding them into the pool of remaining beans. As in the
	 * beginning, the machine starts with one bean at the top.
	 */
	public void repeat() {
		// remove all beans from slots and add to remainingBeans
		// also remove all beans on the Galton Board
		for(int i=0; i < getSlotCount(); i++){
			this.remainingBeans.addAll(this.slots[i]);
			this.slots[i].clear();
			this.remainingBeans.add(this.movingBeans[i]);
			this.movingBeans[i] = null;
		}

		// remove bean from remainingBeans and add to top of Galton board
		if(this.remainingBeans.size() > 0)
			this.movingBeans[0] = this.remainingBeans.remove();
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
		boolean result = false;
		// increment each beans position on Galton Board, only one bean can be at a given y position at a time
		// start from bottom of board and move towards top
		for(int i=(getSlotCount()-1); i >= 0; i--){
			Bean b = movingBeans[i];
			// if there is a bean in this y-position move it appropriately
			if(b != null){
				// if bean is falling off of the last row, add to slot[beans x position], also remove the bean from the Galton board
				if(i == (getSlotCount()-1)){
					this.slots[b.getXPos()].add(b);
				} else {
					// otherwise have the bean choose whether to go left or right, and increment its position in the movingBeans array
					b.choose();
					movingBeans[i+1] = b;
				}
				movingBeans[i] = null;
				result = true;
			} else if(i < (getSlotCount()-1)) {
				// if there was not a bean in that y-position, then there won't be a bean in the next y position
				this.movingBeans[i+1] = null;
			}
		}

		// after finished moving all the beans on the board, add another bean to the top if there are beans remaining
		if(this.remainingBeans.size() > 0){
			this.movingBeans[0] = this.remainingBeans.remove();
		}

		return result;
	}
	
	/**
	 * Number of spaces in between numbers when printing out the state of the machine.
	 * Make sure the number is odd (even numbers don't work as well).
	 */

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
