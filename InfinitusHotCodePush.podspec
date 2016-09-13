Pod::Spec.new do |s|
  s.name         = "InfinitusHotCodePush"
  s.version      = "0.0.1"
  s.summary      = "This tool provides functionality to perform automatic updates of the web based content in your application."
  s.homepage     = "https://github.com/myeveryheart/infinitus-hot-code-push"
  s.license      = "MIT"
  s.author             = { "M" => "myeveryheart@qq.com" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/myeveryheart/infinitus-hot-code-push.git", :tag => "#{s.version}" }
  s.source_files  = "src/ios", "src/ios/**/*.{h,m}"
  s.framework  = "Foundation"
  s.requires_arc = true
end
