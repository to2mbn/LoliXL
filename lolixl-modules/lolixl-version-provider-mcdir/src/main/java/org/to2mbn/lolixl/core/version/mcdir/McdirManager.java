package org.to2mbn.lolixl.core.version.mcdir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.lolixl.core.config.ConfigurationCategory;
import org.to2mbn.lolixl.core.game.version.GameVersionProvider;
import org.to2mbn.lolixl.core.version.mcdir.McdirList.McdirEntry;
import org.to2mbn.lolixl.utils.DictionaryAdapter;
import org.to2mbn.lolixl.utils.ObservableContext;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.layout.Region;

@Service({ ConfigurationCategory.class })
@Properties({
		@Property(name = ConfigurationCategory.PROPERTY_CATEGORY, value = "org.to2mbn.lolixl.core.version.mcdir")
})
@Component
public class McdirManager implements ConfigurationCategory<McdirList> {

	private BundleContext bundleContext;
	private ObservableContext observableContext;

	private McdirList mcdirList = new McdirList();

	@Activate
	public void active(ComponentContext compCtx) {
		bundleContext = compCtx.getBundleContext();
	}

	@Override
	public McdirList store() {
		return mcdirList;
	}

	@Override
	public void restore(McdirList memento) {
		mcdirList.mcdirs.addAll(memento.mcdirs);
		mcdirList.mcdirs.forEach(this::registerMcdir);
	}

	@Override
	public void setObservableContext(ObservableContext ctx) {
		observableContext = ctx;
	}

	@Override
	public Class<? extends McdirList> getMementoType() {
		return McdirList.class;
	}

	@Override
	public ObservableStringValue getLocalizedName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Region createConfiguringPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param path
	 * @return true表示成功，false表示该mcdir已存在
	 */
	public boolean addMcdir(String path) {
		Path absPath = Paths.get(path).toAbsolutePath();
		synchronized (mcdirList.mcdirs) {
			for (McdirEntry entry : mcdirList.mcdirs)
				if (absPath.equals(Paths.get(entry.path).toAbsolutePath()))
					return false;
			McdirEntry entry = new McdirEntry();
			entry.path = path;
			mcdirList.mcdirs.add(entry);
			registerMcdir(entry);
		}
		observableContext.notifyChanged();
		return true;
	}

	/**
	 * @param path
	 * @return true表示成功，false表示该mcdir不存在
	 */
	public boolean removeMcdir(String path) {
		Path absPath = Paths.get(path).toAbsolutePath();
		boolean changed = false;
		synchronized (mcdirList.mcdirs) {
			for (McdirEntry entry : mcdirList.mcdirs)
				if (absPath.equals(Paths.get(entry.path).toAbsolutePath())) {
					mcdirList.mcdirs.remove(entry);
					unregisterMcdir(entry);
					observableContext.notifyChanged();
					changed = true;
					break;
				}
		}
		if (changed)
			observableContext.notifyChanged();
		return changed;
	}

	private void registerMcdir(McdirEntry entry) {
		synchronized (entry) {
			if (entry.registration == null) {
				entry.service = new McdirGameVersionProvider(Paths.get(entry.path));
				Map<String, Object> properties = new HashMap<>();
				properties.put(Constants.SERVICE_RANKING, entry.service.getRanking());
				properties.put(GameVersionProvider.PROPERTY_PROVIDER_LOCATION, "org.to2mbn.lolixl.core.version.mcdir:" + entry.path);
				entry.registration = bundleContext.registerService(GameVersionProvider.class, entry.service, new DictionaryAdapter<>(properties));
			}
		}
	}

	private void unregisterMcdir(McdirEntry entry) {
		synchronized (entry) {
			if (entry.registration != null) {
				entry.registration.unregister();
				entry.registration = null;
				entry.service = null;
			}
		}
	}

}
