package nl.toefel.game.drawer;

import org.eclipse.swt.widgets.Shell;

/**
 * Class that is needed to close SWT drawers from outside the
 * UI tread. display.syncExec causes the UI thread to run this thread
 * and allows disposing the shell!
 * @author Christophe
 *
 */
public class SWTUnloadDrawer implements Runnable {

	Shell shell = null;
	
	public SWTUnloadDrawer(Shell shell){
		this.shell = shell;
	}
	
	public void run() {
		if(shell != null){
			if(!shell.isDisposed())
				shell.dispose();
		}
	}

}
