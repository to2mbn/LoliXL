package org.to2mbn.lolixl.ui;

import java.util.Optional;
import org.to2mbn.lolixl.ui.model.Panel;

/**
 * 提供对应用中打开的面板的管理。
 * <p>
 * 该接口<b>不是</b>线程安全的。
 * 
 * @author yushijinhun
 */
public interface PanelDisplayService {

	void display(Panel panel);

	/**
	 * @return 如果目前没有打开的Panel（即没有Panel可以关闭）则返回{@code true}，反之{@code false}
	 */
	boolean closeCurrent();

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
