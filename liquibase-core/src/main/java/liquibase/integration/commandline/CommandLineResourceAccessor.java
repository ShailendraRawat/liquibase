package liquibase.integration.commandline;

import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Extension of {@link liquibase.resource.ClassLoaderResourceAccessor} that adds extra fuzzy searching logic based on
 * what users may enter that is different than what is exactly correct.
 */
public class CommandLineResourceAccessor extends ClassLoaderResourceAccessor {

    public CommandLineResourceAccessor(ClassLoader loader) {
        super(loader);
    }

    @Override
    public InputStreamList openStreams(String path) throws IOException {
        InputStreamList resourcesAsStream = super.openStreams(path);
        if (resourcesAsStream == null) {
            for (String altPath : getAlternatePaths(path)) {
                resourcesAsStream = super.openStreams(altPath);
                if (resourcesAsStream != null) {
                    return resourcesAsStream;
                }
            }
        }
        return resourcesAsStream;
    }


    @Override
    public SortedSet<String> list(String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        SortedSet<String> contents = new TreeSet<>();
        Set<String> superList = super.list(path, includeFiles, includeDirectories, recursive);
        if (superList != null) {
            contents.addAll(superList);
        }
        for (String altPath : getAlternatePaths(path)) {
            contents.addAll(super.list(altPath, includeFiles, includeDirectories, recursive));
        }
        if (contents.isEmpty()) {
            return null;
        }
        return contents;
    }

    /**
     * Return alternate options for the given path that the user maybe meant. Return in order of likelihood.
     */
    protected List<String> getAlternatePaths(String path) {
        List<String> alternatePaths = new ArrayList<>();

        if (path.startsWith("/")) { //People are often confused about leading slashes in resource paths...
            alternatePaths.add(path.substring(1));
        }

        return alternatePaths;

    }

}
