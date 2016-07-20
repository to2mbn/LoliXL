package org.to2mbn.lolixl.ui;

import java.util.Optional;
import org.to2mbn.lolixl.ui.model.Panel;

/**
 * 提供对应用中面板的管理。
 * 
 * @author yushijinhun
 */
public interface PanelService {

	Panel newPanel();

	/**
	 * 获取当前打开的Panel。
	 * <p>
	 * 该方法可以从任意线程访问。
	 * 
	 * @return 当前打开的Panel
	 */
	Optional<Panel> getCurrent();

	/**
	 * 获取目前打开的所有Panel。
	 * <p>
	 * index越小代表该Panel越靠前。例如，{@code openedPanels[0]}代表最顶层的Panel，
	 * {@code openedPanels[openedPanels.length - 1]}代表最底层的Panel。<br>
	 * 该方法可以从任意线程访问。
	 * 
	 * @return 目前打开的所有Panel
	 */
	Panel[] getOpenedPanels();

}
