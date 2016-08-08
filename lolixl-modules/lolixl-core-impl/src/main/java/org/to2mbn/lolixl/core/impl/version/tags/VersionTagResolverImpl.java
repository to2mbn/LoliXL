package org.to2mbn.lolixl.core.impl.version.tags;

import java.util.Set;
import java.util.TreeSet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.to2mbn.jmccc.version.Version;
import org.to2mbn.lolixl.core.game.version.tags.VersionTag;
import org.to2mbn.lolixl.core.game.version.tags.VersionTagResolver;
import org.to2mbn.lolixl.core.game.version.tags.VersionTagSolution;
import org.to2mbn.lolixl.utils.ObservableServiceTracker;
import javafx.beans.binding.SetBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

@Service({ VersionTagResolver.class })
@Component
public class VersionTagResolverImpl implements VersionTagResolver {

	private ObservableServiceTracker<VersionTagSolution> solutions;

	@Activate
	public void active(ComponentContext compCtx) {
		solutions = new ObservableServiceTracker<>(compCtx.getBundleContext(), VersionTagSolution.class);
		solutions.open(true);
	}

	@Deactivate
	public void deactive() {
		solutions.close();
	}

	@Override
	public ObservableSet<VersionTag> resolveTags(Version version) {
		return new SetBinding<VersionTag>() {

			{
				bind(solutions.getServiceList());
			}

			@Override
			protected ObservableSet<VersionTag> computeValue() {
				Set<VersionTag> result = new TreeSet<>();
				solutions.getServiceList().forEach(solution -> solution.resolve(version, result));
				return FXCollections.unmodifiableObservableSet(FXCollections.observableSet(result));
			}
		};
	}

}
