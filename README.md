![everestheader](https://user-images.githubusercontent.com/23148259/39124644-c886b47a-4719-11e8-953c-f079b3edb664.png)

----

Everest _(formerly RESTaurant)_ is an upcoming REST API testing client written in JavaFX.

![default](https://user-images.githubusercontent.com/23148259/39123684-2f7df138-4716-11e8-8ff8-589a1cc47834.PNG)
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

![get](https://user-images.githubusercontent.com/23148259/39123790-8a828b34-4716-11e8-8913-62a6356dd36e.PNG)	
_Making a GET request with Everest._

# Planned features
- Ability to make requests with the common HTTP methods. _(duh)_
- **Syntax highlighting**: I had tried using [RichTextFX](https://github.com/FXMisc/RichTextFX) for my text editor, 
  [Ballad](https://github.com/RohitAwate/Ballad), and failed. And I really don't wish to embed an
  online editor like Ace into Everest. So, for the time being, I'm gonna use JavaFX's inbuilt TextArea which doesn't offer syntax highlighting.
- **History tab**, for quickly looking at your request history. 🕒
- **API Authentication**: This is of course a primary feature of any REST client, but currently, not something I've done or used before.
  So I will need some time to learn this stuff.
- Theming support with JavaFX CSS. 🌈
- **Multi-tab layout** to unleash your internal REST wizard!
  
_That's all the core stuff that I can think of at the moment. Will keep updating this as we go._

## Not-so-certain features
- Text prediction for the address bar based on your history.
- Postman has this really cool feature wherein it can create a **mock server**. I'll do that once the core features are implemented and if it doesn't severely impact performance.


![error](https://user-images.githubusercontent.com/23148259/39123851-d52d250e-4716-11e8-9c52-800fd708dc2b.PNG)


# Features currently live
- GET, POST, PUT, DELETE and PATCH requests.
- Adding request headers, query parameters and the request body (raw, octet-stream, URL-encoded or form-data)
- Response details (HTTP status code, response time, response size)
- **Custom themes!** Changing themes needs a manual edit to the settings.json file. This will be properly integrated into a Settings menu later.
- **Multi-tab layout**. I'm battling with JavaFX to allow me to add a simple '+' button for adding a new tab. So far, not successful. However, you can use the Ctrl+T keyboard shortcut.
- **History tab!** All the requests you ever made are now shown to the left of the app.
- Everest now maintains its state between sessions so you don't lose your work.

![search](https://user-images.githubusercontent.com/23148259/39123910-19ddeb8e-4717-11e8-9827-84ad53c5f16f.PNG)

_Everest's search feature in action._

# Releases
The first alpha release is available [here](https://github.com/RohitAwate/Everest/releases/tag/Alpha-1.0).
Make sure you read the release notes to better understand the release.

# Suggestions and improvements
Use these options to reach me:
- Open a GitHub issue.
- Email me at rohitawate121@gmail.com.
- Tweet me at [@TheRohitAwate](https://twitter.com/TheRohitAwate)


Aaaand, that's it!
I don't think anyone will make it down here. But if you did, you're awesome! 🏆

_Cheers!_
