import main.MainApp;
import org.junit.Assert;
import org.junit.Test;

public class TestArrays {
    @Test
    public void test1() {
        int[] arr1 = {1, 7};
        int[] arr2 = {1, 2, 4, 4, 2, 3, 4, 1 , 7};
        Assert.assertArrayEquals(arr1, MainApp.getNewArray(arr2));
    }

    @Test
    public void test2() {
        int[] arr1 = {0};
        int[] arr2 = {1, 2, 4, 4, 4, 4, 4, 4 , 0};
        Assert.assertArrayEquals(arr1, MainApp.getNewArray(arr2));
    }

    @Test (expected =  RuntimeException.class)
    public void test3() {
        int[] arr1 = {1, 2, 3, 3, 3, 7};
        MainApp.getNewArray(arr1);
    }

    @Test
    public void test5() {
        int[] arr1 = {1, 1, 1};
        Assert.assertEquals(false, MainApp.testArray(arr1));
    }

    @Test
    public void test6() {
        int[] arr1 = {4, 4, 4, 4, 4};
        Assert.assertEquals(false, MainApp.testArray(arr1));
    }

    @Test
    public void test7() {
        int[] arr1 = {4, 4, 1, 4, 3};
        Assert.assertEquals(false, MainApp.testArray(arr1));
    }

    @Test
    public void test8() {
        int[] arr1 = {4, 4, 1, 4, 1};
        Assert.assertEquals(true, MainApp.testArray(arr1));
    }
}
