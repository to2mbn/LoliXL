package org.to2mbn.lolixl.ui;

import java.util.Optional;

/**
 * 默认侧边栏的侧边栏面板的显示服务
 * 默认侧边栏由内容栏和侧边栏面板构成，侧边栏面板为内容栏服务
 * 如内容栏中的玩家信息一栏，点击后侧边栏面板将出现详细的玩家账户设置面板，
 * <p>
 * 侧边栏面板与{@link PanelDisplayService}的面板的不同在于:
 * 1.前者不会占满整个窗口的内容栏
 * 2.不能叠加，如果在显示了A面板同时调用显示B面板，A面板将会被关闭
 */
public interface SideBarPanelDisplayService extends PanelFactory {

	/**
	 * 获取当前打开的Panel。
	 * <p>
	 * 该方法可以从任意线程访问。
	 *
	 * @return 当前打开的Panel
	 */
	Optional<Panel> getCurrent();
}
