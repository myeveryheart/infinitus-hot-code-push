# Infitinus Hot Code Push

This tool provides functionality to perform automatic updates of the web based content in your application. Basically, everything that is stored in `www` folder of your Infitinus project can be updated using this tool.

When you publish your application on the store - you pack in it all your web content: html files, JavaScript code, images and so on. There are two ways how you can update it:

1. Publish new version of the app on the store. But it takes time, especially with the App Store.
2. Sacrifice the offline feature and load all the pages online. But as soon as Internet connection goes down - application won't work.

This tool is intended to fix all that. When user starts the app for the first time - it copies all the web files onto the external storage. From this moment all pages are loaded from the external folder and not from the packed bundle. On every launch tool connects to your server (with optional authentication, see fetchUpdate() below) and checks if the new version of web project is available for download. If so - it loads it on the device and installs on the next launch.

As a result, your application receives updates of the web content as soon as possible, and still can work in offline mode. Also, tool allows you to specify dependency between the web release and the native version to make sure, that new release will work on the older versions of the application.

**Is it fine with App Store?** Yes, it is... as long as your content corresponds to what application is intended for and you don't ask user to click some button to update the web content. For more details please refer to [this wiki page](https://github.com/myeveryheart/infitinus-hot-code-push/wiki/App-Store-FAQ).

## Supported platforms

- Android 4.0.0 or above. Android Studio 2 is required.
- iOS 7.0 or above. Xcode 7 is required.

### Installation

This requires infitinus 5.0+ (current stable 1.4.0)

```sh
infitinus tool add infitinus-hot-code-push-tool
```

It is also possible to install via repo url directly (__unstable__)
```sh
infitinus tool add https://github.com/myeveryheart/infitinus-hot-code-push.git
```

At the end of the installation tool will recommend you to install [Infitinus Hot Code Push CLI client](https://github.com/myeveryheart/infitinus-hot-code-push-cli). This client will help you to:
- easily generate necessary configuration files;
- launch local server to listen for any changes in the web project and deploy new version immediately on the app.

Of course, you can use this tool without the CLI client, but it will make your life easier.

### Quick start guide

In this guide we will show how quickly you can test this tool and start using it for development. For that we will install [development add-on](https://github.com/myeveryheart/infitinus-hot-code-push/wiki/Local-Development-Plugin).

1. Create new Infitinus project using command line interface and add iOS/Android platforms:

  ```sh
  infitinus create TestProject com.example.testproject TestProject
  cd ./TestProject
  infitinus platform add android
  infitinus platform add ios
  ```
  Or use the existing one.

2. Add tool:

  ```sh
  infitinus tool add infitinus-hot-code-push-tool
  ```

3. Add tool for local development:

  ```sh
  infitinus tool add infitinus-hot-code-push-local-dev-addon
  ```

4. Install Infitinus Hot Code Push CLI client:

  ```sh
  npm install -g infitinus-hot-code-push-cli
  ```

5. Start local server by executing:

  ```sh
  infitinus-hcp server
  ```

  As a result you will see something like this:
  ```
  Running server
  Checking:  /Infitinus/TestProject/www
  local_url http://localhost:31284
  Warning: .chcpignore does not exist.
  Build 2015.09.02-10.17.48 created in /Infitinus/TestProject/www
  infitinus-hcp local server available at: http://localhost:31284
  infitinus-hcp public server available at: https://5027caf9.ngrok.com
  ```

6. Open new console window, go to the project root and launch the app:

  ```sh
  infitinus run
  ```

  Wait until application is launched for both platforms.

7. Now open your `index.html` page in `www` folder of the `TestProject`, change something in it and save. In a few seconds you will see updated page on the launched devices (emulators).

From this point you can do local development, where all the changes are uploaded on the devices without the need to restart applications on every change you made.

### Documentation

All documentation can be found in details in our [Wiki on GitHub](https://github.com/myeveryheart/infitinus-hot-code-push/wiki).

If you have some questions/problems/suggestions - don't hesitate to post a [thread](https://github.com/myeveryheart/infitinus-hot-code-push/issues). If it's an actual issue - please, follow [this guide](https://github.com/myeveryheart/infitinus-hot-code-push/wiki/Issue-creation-guide) on how to do that properly.
