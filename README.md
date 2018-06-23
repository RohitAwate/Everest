![everestheader](https://user-images.githubusercontent.com/23148259/39124644-c886b47a-4719-11e8-953c-f079b3edb664.png)

Everest _(formerly RESTaurant)_ is an upcoming REST API testing client written in JavaFX.

![idle](https://user-images.githubusercontent.com/23148259/39201973-416978a6-480e-11e8-8f94-ddd656ea8784.PNG)
_Everest running on Windows 10._
# Why Everest?
- Unlike other REST clients like Postman and Insomnia, Everest is written in Java. Thus, it is significantly
  **lighter on resources and more responsive** than the Electron-based options. The goal with Everest is to provide pretty much the same
  level of functionality as the other options, but in a lighter, native but equally slick package. It is a big and slightly arrogant bet, 
  but then I don't think anyone other than me would use this! So heck, at least I'll learnt a lot. _Not bad for a Comp Sci student, eh?_
- In all of my projects, design is of paramount importance. And Everest is no exception.
  With a **gorgeous, flat design and a neutral color scheme**, Everest is a pleasure to look at and to work with.
  _I want you to want to use it!_
- Being a Java application, Everest is inherently **cross-platform**. It will run anywhere there's a JVM.
- Everest is powered by **Jersey**, the robust and powerful Java library to build and consume RESTful web services.

![get](https://user-images.githubusercontent.com/23148259/41772368-f9276dae-7635-11e8-97ec-1a1e8aa608c4.PNG)
_Making a GET request with Everest._

# Features currently live
- GET, POST, PUT, DELETE and PATCH requests.
- Adding request headers, query parameters and the request body (raw, octet-stream, URL-encoded or form-data)
- Response details (HTTP status code, response type, time, size)
- **Custom themes**: I encourage you to develop new themes by taking reference of [Adreana](https://github.com/RohitAwate/RESTaurant/blob/master/src/main/resources/css/Adreana.css). I plan on documenting it better in the future. Changing themes needs a manual edit to `Everest/config/settings.json`. This will be properly integrated into a Settings menu soon. 🌈
- **Multi-tab layout**: I'm battling with JavaFX to allow me to add a simple '+' button for adding a new tab. So far, not successful. However, you can use the Ctrl+T keyboard shortcut.
- **History tab**: All the requests you ever made are now shown to the left of the app. 🕒
- **Searching through your request history**: Everest searches not just on the basis of the request target but all other parameters that form the request including headers, the request body, query parameters and even file names. It will **intelligently rank** the results on the basis of their relevance. 🔎
- Everest now maintains its state between sessions so you don't lose your work. 🧠
- **Response Visualizer**: You can now view JSON responses just as you would your file tree.
- **Syntax highlighting**: Everest now highlights your JSON, XML and HTML responses and requests, powered by [RichTextFX](https://github.com/FXMisc/RichTextFX). You can add new CSS syntax themes in `Everest/themes/syntax/` by taking reference of the default, [Moondust](https://github.com/RohitAwate/Everest/blob/master/src/main/resources/css/syntax/Moondust.css).

![search](https://user-images.githubusercontent.com/23148259/39201474-cc4e6e2e-480c-11e8-8770-7fc4401a2435.PNG)

_Everest's search feature in action._

# Upcoming features
- **API Authentication**: This is of course a primary feature of any REST client, but currently, not something I've done or used before.
  So I will need some time to learn this stuff.
- **Text prediction** for the address bar based on your history.
- _Postman_ has this really cool feature wherein it can create a **mock server**. I'll do that once the core features are implemented and if it doesn't severely impact performance.

![Visualizer](https://user-images.githubusercontent.com/23148259/41772225-5bafa988-7635-11e8-9f44-b0e4aa4ea05b.png)
_Everest's convenient Visualizer makes reading response bodies a breeze!_

# Keymap
| Shortcut     | Task                     |
|--------------|--------------------------|
| Ctrl + T     | New Tab                  |
| Ctrl + W     | Close tab                |
| Ctrl + H     | Toggle History           |
| Ctrl + Enter | Send request             |
| Ctrl + L     | Focus address bar        |
| Ctrl + M     | Open HTTP Method box     |
| Ctrl + F     | Focus history search bar |
| Alt + P      | Focus Params tab         |
| Alt + A      | Focus Authentication tab |
| Alt + H      | Focus Headers tab        |
| Alt + B      | Focus Body tab           |

# Releases 🚀
Everest is under active development and you can get the latest alpha build from [Releases](https://github.com/RohitAwate/Everest/releases). Make sure you read the release notes to understand what works and what doesn't, how to report issues and how to run the binary.

# Building from source
Everest uses Maven, so building from the source code is very simple. You need to have JDK 8 (9 should also work) installed, along with Maven and Git. If you're using OpenJDK, you will separately need to install OpenJFX. Once you have everything set up, follow these simple steps:
1. Clone the repository: `git clone https://github.com/RohitAwate/Everest.git`
2. Enter the repository: `cd Everest`
3. Build a binary: `mvn package`
4. Run the binary: `java -jar target/Everest-Alpha-1.X.jar` replacing 'X' with the current version.

# Suggestions and improvements
Use these options to reach me:
- Open a GitHub issue.
- Email me at rohitawate121@gmail.com.
- Tweet me [@TheRohitAwate](https://twitter.com/TheRohitAwate).


Aaaand, that's it!
I don't think anyone will make it down here. But if you did, you're awesome! 🏆

_Cheers!_
