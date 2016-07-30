package org.to2mbn.lolixl.ui.theme.loading;

import org.to2mbn.lolixl.ui.theme.Theme;
import org.to2mbn.lolixl.ui.theme.exception.InvalidThemeException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public interface ThemeLoadingService {
	Optional<Theme> loadAndPublish(URL url) throws IOException, InvalidThemeException;
}
