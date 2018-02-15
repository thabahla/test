package za.co.interfile.bas;

/**
 * @author Theuns Cloete
 */
public interface Task extends Runnable {

    void requestShutdown();
}
