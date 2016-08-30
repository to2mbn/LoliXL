package org.to2mbn.lolixl.ui.panel;

import java.util.Optional;

/**
 * 提供对应用中面板的管理。
 * 
 * @author yushijinhun
 */
public interface PanelDisplayService extends PanelFactory {

	/**
	 * 获取当前打开的Panel。
	 * 
	 * @return 当前打开的Panel
	 */
	Optional<Panel> getCurrent();

	/**
	 * 获取目前打开的所有Panel。
	 * <p>
	 * index越小代表该Panel越靠前。例如，{@code openedPanels[0]}代表最顶层的Panel，
	 * {@code openedPanels[openedPanels.length - 1]}代表最底层的Panel。
	 * 
	 * @return 目前打开的所有Panel
	 */
	Panel[] getOpenedPanels();

}
