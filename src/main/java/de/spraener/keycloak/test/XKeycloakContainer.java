package de.spraener.keycloak.test;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * <p>
 *     An extendable keycloak testcontainer able to run in an extension in a gradle environment.
 * </p>
 * <p>
 *     It also opens the opportunity to add your own configuration (for debugging keycloak etc)
 * </p>
 */
public class XKeycloakContainer extends ExtendableKeycloakContainer<XKeycloakContainer> {
    private static final String DEFAULT_KEYCLOAK_PROVIDERS_NAME = "providers.jar";
    private static final String DEFAULT_KEYCLOAK_PROVIDERS_LOCATION = "/opt/keycloak/providers";

    private List<Consumer<ExplodedImporter>> providerJarModifierList = null;
    private List<BiConsumer<ExtendableKeycloakContainer<?>, List<String>>> configurationModifiers = null;

    public XKeycloakContainer() {
        super();
    }

    public XKeycloakContainer(String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    protected void configure() {
        super.configure();
        List<String> commandParts = new ArrayList<>(Arrays.asList(super.getCommandParts()));

        if( this.providerJarModifierList !=null && !this.providerJarModifierList.isEmpty() ) {
            createKeycloakExtensionDeployment(DEFAULT_KEYCLOAK_PROVIDERS_LOCATION, DEFAULT_KEYCLOAK_PROVIDERS_NAME);
        }
        if( this.configurationModifiers!=null ) {
            for (BiConsumer<ExtendableKeycloakContainer<?>, List<String>> configurationModifier : this.configurationModifiers) {
                configurationModifier.accept(this, commandParts);
            }
        }
        super.setCommandParts(commandParts.toArray(new String[0]));
    }

    public XKeycloakContainer withDebugPort(int hostPort, int containerPort) {
        addFixedExposedPort(hostPort, containerPort);
        return self();
    }

    public XKeycloakContainer withProviderJarContentModifier(Consumer<ExplodedImporter> importerModifier) {
        if( providerJarModifierList ==null ) {
            this.providerJarModifierList = new ArrayList<>();
        }
        this.providerJarModifierList.add(importerModifier);
        return self();
    }

    public XKeycloakContainer withConfigurationModifier(BiConsumer<ExtendableKeycloakContainer<?>, List<String>> configurationModifier) {
        if( this.configurationModifiers==null ) {
            this.configurationModifiers = new ArrayList<>();
        }
        this.configurationModifiers.add(configurationModifier);
        return self();
    }

    protected void createKeycloakExtensionDeployment(String deploymentLocation, String extensionName) {
        requireNonNull(deploymentLocation, "deploymentLocation must not be null");
        requireNonNull(extensionName, "extensionName must not be null");
        requireNonNull(providerJarModifierList, "jarContentModifiers must be added");

        if (!providerJarModifierList.isEmpty()) {
            final File file;
            try {
                file = Files.createTempFile("keycloak", ".jar").toFile();
                file.setReadable(true, false);
                file.deleteOnExit();
                ExplodedImporter importer = ShrinkWrap.create(JavaArchive.class, extensionName)
                        .as(ExplodedImporter.class)
                        ;
                if( this.providerJarModifierList != null ) {
                    for (Consumer<ExplodedImporter> explodedImporterConsumer : this.providerJarModifierList) {
                        explodedImporterConsumer.accept(importer);
                    }
                }
                importer
                        .as(ZipExporter.class)
                        .exportTo(file, true);
                withCopyFileToContainer(MountableFile.forHostPath(file.getAbsolutePath()), deploymentLocation + "/" + extensionName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
