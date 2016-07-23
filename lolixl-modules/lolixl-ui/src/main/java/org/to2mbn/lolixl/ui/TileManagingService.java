package org.to2mbn.lolixl.ui;

import org.to2mbn.lolixl.ui.component.Tile;

public interface TileManagingService {
	/**
	 * 描述一个磁贴的状态
	 * <p>
	 * 当磁贴过多时 多出的磁贴会被设置为HIDDEN 从磁贴容器隐藏
	 * 隐藏的磁贴会出现在{@see HiddenTilesPanelContentView}面板中
	 * <p>
	 * <code>COMMON</code>为<code>HIDDEN<code>和</code>SHOWN</code>两种状态的统称
	 */
	enum TileStatus {
		HIDDEN, SHOWN, COMMON
	}

	/**
	 * 磁贴的容器宽度只有80
	 */
	int TILE_MAX_SIZE = 80;

	/**
	 * 向容器内添加磁贴
	 * 对于已经满了的容器 新加入的磁贴会被隐藏
	 * <p>
	 * 需要在JavaFX线程运行
	 *
	 * @param tile
	 */
	void addTile(Tile tile);

	/**
	 * 自动为给定的面板对象创建对应的磁贴 并加入容器中
	 * <p>
	 * 需要在JavaFX线程运行
	 *
	 * @param panel
	 */
	void addTileForPanel(Panel panel);

	/**
	 * 删除磁贴
	 * 该磁贴可能在容器中也可能被隐藏
	 * <p>
	 * 需要在JavaFX线程运行
	 *
	 * @param panel
	 */
	void removeTile(Panel panel);

	/**
	 * 设置所有磁贴的大小
	 * <b>需要在JavaFX线程运行</b>
	 *
	 * @param size 最大不超过{@see TileManagingService.TILE_MAX_SIZE}
	 * @throws IllegalArgumentException 如果size超过{@see TileManagingService.TILE_MAX_SIZE}
	 */
	void setSize(int size);

	int getSize();

	/**
	 * 依照给定的TileStatus获取磁贴
	 *
	 * @param status
	 * @return 给定TileStatus下所有符合条件的磁贴
	 * @see TileManagingService.TileStatus
	 */
	Tile[] getTiles(TileStatus status);

	/**
	 * 更新磁贴的顺序
	 * 按照给定的tiles的从索引0开始的顺序从上到下重新排列容器内的磁贴
	 * 在从上到下的顺序中 最上方的若干个会被容器显示 而剩余的会被隐藏 放置在{@see HiddenTilesPanelContentView}面板中
	 * <p>
	 * 如果给定的tiles中存在容器中不存在的磁贴 则忽略此索引
	 * 给定的tiles必须包含所有容器内包含的磁贴
	 * <p>
	 * <b>需要在JavaFX线程运行</b>
	 *
	 * @param tiles
	 */
	void updateTilesOrder(Tile[] tiles);
}
