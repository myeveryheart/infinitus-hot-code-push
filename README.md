# Infinitus Hot Code Push

This library provides functionality to perform automatic updates of the web based content in your application. Basically, everything that is stored in `www` folder of your Infinitus project can be updated using this library.

When you publish your application on the store - you pack in it all your web content: html files, JavaScript code, images and so on. There are two ways how you can update it:

1. Publish new version of the app on the store. But it takes time, especially with the App Store.
2. Sacrifice the offline feature and load all the pages online. But as soon as Internet connection goes down - application won't work.

This library is intended to fix all that. When user starts the app for the first time - it copies all the web files onto the external storage. From this moment all pages are loaded from the external folder and not from the packed bundle. On every launch library connects to your server (with optional authentication, see fetchUpdate() below) and checks if the new version of web project is available for download. If so - it loads it on the device and installs on the next launch.

As a result, your application receives updates of the web content as soon as possible, and still can work in offline mode. Also, library allows you to specify dependency between the web release and the native version to make sure, that new release will work on the older versions of the application.

**Is it fine with App Store?** Yes, it is... as long as your content corresponds to what application is intended for and you don't ask user to click some button to update the web content. For more details please refer to [this wiki page](https://github.com/myeveryheart/infinitus-hot-code-push/wiki/App-Store-FAQ).

## Supported platforms

- Android 4.0.0 or above. Android Studio 2 is required.
- iOS 7.0 or above. Xcode 7 is required.

### Installation with CocoaPods for iOS

#### Podfile

To integrate InfinitusHotCodePush into your Xcode project using CocoaPods, specify it in your `Podfile`:

```ruby
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '7.0'

target 'TargetName' do
pod 'InfinitusHotCodePush'
end
```

Then, run the following command:

```bash
$ pod install
```

I recommend you to install [Infinitus Hot Code Push CLI client](https://github.com/myeveryheart/infinitus-hot-code-push-cli). This client will help you to easily generate necessary configuration files;

Of course, you can use this library without the CLI client, but it will make your life easier.

## Usage

### HCPHelper

`HCPHelper` manages an all task.

#### Creating a HCPHelper Instance

```objective-c
NSURL *webUrl = [NSURL URLWithString:@"http://example.com"];
HCPHelper *hcpHelper = [[HCPHelper alloc] initWithWebUrl:webUrl];
```

#### Creating a Fetch Task

```objective-c
[hcpHelper fetchUpdate:^(BOOL needUpdate, NSError *error)
{

}
```

#### Creating a Download Task

```objective-c
[hcpHelper downloadUpdate:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error)
{

}
```

#### Creating a Install Task

```objective-c
[hcpHelper installUpdate:^(BOOL success, NSError *error)
{
  
}
```

#### Get The local www Path

```objective-c
NSURL *wwwUrl = [hcpHelper pathToWww];
```

#### Load The www From External Storage Folder Or Not

```objective-c
BOOL isLoadFromExternalStorageFolder = [hcpHelper loadFromExternalStorageFolder];
```

### Documentation

All documentation can be found in details in our [Wiki on GitHub](https://github.com/myeveryheart/infinitus-hot-code-push/wiki).

If you have some questions/problems/suggestions - don't hesitate to post a [thread](https://github.com/myeveryheart/infinitus-hot-code-push/issues). If it's an actual issue - please, follow [this guide](https://github.com/myeveryheart/infinitus-hot-code-push/wiki/Issue-creation-guide) on how to do that properly.
