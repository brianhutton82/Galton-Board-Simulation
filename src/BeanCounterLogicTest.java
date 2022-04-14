import gov.nasa.jpf.vm.Verify;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
//import org.junit.*;
//import org.mockito.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions
	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		if (Config.getTestType() == TestType.JUNIT) {
			slotCount = 5;
			beanCount = 3;
			isLuck = true;
		} else if (Config.getTestType() == TestType.JPF_ON_JUNIT) {
			/*
			 * TODO: Use the Java Path Finder Verify API to generate choices for slotCount,
			 * beanCount, and isLuck: slotCount should take values 1-5, beanCount should
			 * take values 0-3, and isLucky should be either true or false. For reference on
			 * how to use the Verify API, look at:
			 * https://github.com/javapathfinder/jpf-core/wiki/Verify-API-of-JPF
			 */
			slotCount = Verify.getInt(1, 5);
			beanCount = Verify.getInt(0, 3);
			isLuck = Verify.getBoolean();
		} else {
			assert (false);
		}

		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random(42));
		}

		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount
				+ ", beanCount=" + beanCount + ", isLucky=" + isLuck + "):";
	}

	@AfterClass
	public static void tearDown() {
	}

	/**
	 * Test case for void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		// TODO: Implement
		/*
		 * Currently, it just prints out the failString to demonstrate to you all the
		 * cases considered by Java Path Finder. If you called the Verify API correctly
		 * in setUp(), you should see all combinations of machines
		 * (slotCount/beanCount/isLucky) printed here:
		 * 
		 * Failure in (slotCount=1, beanCount=0, isLucky=false):
		 * Failure in (slotCount=1, beanCount=0, isLucky=true):
		 * Failure in (slotCount=1, beanCount=1, isLucky=false):
		 * Failure in (slotCount=1, beanCount=1, isLucky=true):
		 * ...
		 * 
		 * PLEASE REMOVE when you are done implementing.
		 */

		logic.reset(beans);
		if(beanCount > 0){
			assertEquals(beanCount - 1, logic.getRemainingBeanCount());
			for (int i = 0; i < slotCount; i++) {
				assertEquals(0, logic.getSlotBeanCount(i));
				int xpos = logic.getInFlightBeanXPos(i);
				if (i > 0) {
					assertEquals(BeanCounterLogic.NO_BEAN_IN_YPOS, xpos);
				} else {
					assertNotEquals(BeanCounterLogic.NO_BEAN_IN_YPOS, xpos);
				}
			}
			
		} else {
			assertEquals(0, logic.getRemainingBeanCount());
			for (int i = 0; i < slotCount; i++) {
				int xpos = logic.getInFlightBeanXPos(i);
				assertEquals(0, logic.getSlotBeanCount(i));
				assertNotEquals(BeanCounterLogic.NO_BEAN_IN_YPOS, xpos);
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {
			for (int i = 0; i < slotCount; i++) {
				int xpos = logic.getInFlightBeanXPos(i);
				if (xpos >= 0) {
					assertTrue(xpos <= i);
				}
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		// TODO: Implement
		logic.reset(beans);
		while (logic.advanceStep()) {
			int currentCount = logic.getRemainingBeanCount();
			for (int i = 0; i < slotCount; i++) {
				currentCount += logic.getSlotBeanCount(i);
				if(logic.getInFlightBeanXPos(i) != BeanCounterLogic.NO_BEAN_IN_YPOS){
					currentCount += 1;
				}
				total += logic.getSlotBeanCount(i);
			}
			assertEquals(currentCount, beanCount);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		// TODO: Implement
		logic.reset(beans);
		while(logic.advanceStep());
		assertEquals(logic.getRemainingBeanCount(), 0);
		int total = 0;
		for(int i=0; i < slotCount; i++){
			assertEquals(logic.getInFlightBeanXPos(i), -1);
			total += logic.getSlotBeanCount(i);
		}
		assertEquals(total, beanCount);
	}
	
	/**
	 * Test case for void lowerHalf()().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		// TODO: Implement
		logic.reset(beans);
		while(logic.advanceStep());
		int[] slots = new int[slotCount];
		for(int i=0; i < slotCount; i++){
			slots[i] = logic.getSlotBeanCount(i);
		}

		int halfCount = beanCount / 2;

		for(int i=slotCount-1; i > 0; i--){
			if(halfCount - logic.getSlotBeanCount(i) > -1){
				slots[i] = 0;
				halfCount -= logic.getSlotBeanCount(i);
			} else {
				slots[i] -= halfCount;
				break;
			}
		}

		logic.lowerHalf();
		int total = 0;
		for(int i=0; i < slotCount; i++){
			assertEquals(slots[i], logic.getSlotBeanCount(i));
		}
	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.upperHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
		logic.reset(beans);
		while(logic.advanceStep());
		int[] slots = new int[slotCount];
		for(int i=0; i < slotCount; i++){
			slots[i] = logic.getSlotBeanCount(i);
		}

		int halfCount = beanCount / 2;

		for(int i=0; i < slotCount; i++){
			if(halfCount - logic.getSlotBeanCount(i) > -1){
				slots[i] = 0;
				halfCount -= logic.getSlotBeanCount(i);
			} else {
				slots[i] -= halfCount;
				break;
			}
		}

		logic.upperHalf();
		int total = 0;
		for(int i=0; i < slotCount; i++){
			assertEquals(slots[i], logic.getSlotBeanCount(i));
		}
	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: The machine is operating in skill mode.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: Bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {
		if (!isLuck) {
			// TODO: Implement
			logic.reset(beans);
			while(logic.advanceStep());
			int[] first = new int[slotCount];
			for(int i=0; i < slotCount; i++){
				first[i] = logic.getSlotBeanCount(i);
			}
			logic.repeat();
			while(logic.advanceStep());
			for(int i=0; i < slotCount; i++){
				assertEquals(logic.getSlotBeanCount(i), first[i]);
			}
		}
	}
}
