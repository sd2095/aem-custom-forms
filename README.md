# AEM Custom Forms Project

This is a sample forms project for AEM-based applications. It is intended as a best-practice set of examples as well as a potential starting point to develop your own functionality on form components.

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /conf) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs. This project have experience fragment templates, style systems, editable template policies, customizable form components.

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with

    mvn clean install -PautoInstallPackage,adobe-public

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish,adobe-public

Or alternatively

    mvn clean install -PautoInstallPackage,adobe-public -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle,adobe-public


## ClientLibs

The frontend module is made available using an [AEM ClientLib](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/clientlibs.html). When executing the NPM build script, the app is built and the [`aem-clientlib-generator`](https://github.com/wcm-io-frontend/aem-clientlib-generator) package takes the resulting build output and transforms it into such a ClientLib.

A ClientLib will consist of the following files and directories:

- `css/`: CSS / LESS files which can be requested in the HTML
- `css.txt` (tells AEM the order and names of files in `css/` so they can be merged)
- `js/`: JavaScript files which can be requested in the HTML
- `js.txt` (tells AEM the order and names of files in `js/` so they can be merged

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
