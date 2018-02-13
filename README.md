# RESTaurant
RESTaurant is an upcoming native REST client written in JavaFX. It is currently in the initial stages of development.

![restaurant](https://user-images.githubusercontent.com/23148259/36142398-fd032968-10cd-11e8-8eea-840adb28fe21.PNG)

# Why RESTaurant?
- Unlike other REST clients like Postman and Insomnia, RESTaurant is a **native** application written in Java. Thus, it is significantly
  **lighter on resources and more responsive** than the Electron-based options. The goal with RESTaurant is to provide pretty much the same
  level of functionality as the formerly mentioned options, but in a lighter, native but equally slick package. It is a big and slightly arrogant bet, 
  but then I don't think anyone other than me would use this! So heck, at least I'll learnt a lot. _Not bad for a Comp Sci student, eh?_
- In all of my projects, design is of paramount importance. And RESTaurant is no exception.
  With a **gorgeous, flat design and a neutral color scheme**, RESTaurant is a pleasure to look at and to work with.
  _I want you to want to use it!_
- RESTaurant is powered by **Jersey**, the robust and powerful Java library to build and consume RESTful web services.

![restaurant2](https://user-images.githubusercontent.com/23148259/36142456-2cd72658-10ce-11e8-872a-5c2e3eedd398.PNG)

# Planned features
- Ability to make requests with the common HTTP methods. _(duh)_
- **Syntax highlighting**: I had tried using [RichTextFX](https://github.com/FXMisc/RichTextFX) for my text editor, 
  [Ballad](https://github.com/RohitAwate/Ballad), and failed. And I really don't wish to embed an
  online editor like Ace into RESTaurant. So, for the time being, I'm gonna use JavaFX's inbuilt TextArea which doesn't offer syntax highlighting.
- **History tab**, for quickly looking at your request history. 🕒
- **API Authentication**: This is of course a primary feature of any REST client, but currently, not something I've done or used before.
  So I will need some time to learn this stuff.
- Theming support with JavaFX CSS. 🌈
- **Multi-tab layout** to unleash your internal REST wizard!
  
_That's all the core stuff that I can think of at the moment. Will keep updating this as we go._

## Not-so-certain features
- Suggestions as you type in the address field.
- Postman has this really cool feature wherein it can create a **mock server**. Maybe do that.


![restaurant3](https://user-images.githubusercontent.com/23148259/36142488-3fea727c-10ce-11e8-8df1-2b7fd3d59d98.PNG)


# Features currently live
- Adding request headers and the request body (raw, octet-stream, URL-encoded or form-data)
- GET, POST, PUT and DELETE requests
- Response details (HTTP status code, response time, response size)
- Custom themes! Changing themes needs a manual edit to the settings.json file. This will be properly integrated into a Settings menu later.
- Multi-tab layout. I'm battling with JavaFX to allow me to add a simple '+' button for adding a new tab. So far, not successful. However, you can use the Ctrl+T keyboard shortcut.
- Requests history gets saved to a database. History tab will be coming up soon.

# Releases
I mean, it's open-source. You can build yourself a 'release' right now!

Jokes aside, I'm planning to release an alpha once the GET, POST, PUT and DELETE methods are up and running.
I'll also add the aforementioned history tab as a bonus, maybe!

**Alpha ETA**: March 2018 _(hopefully)_

Aaaand, that's it!
I don't think anyone will make it down here. But if you did, you're awesome and thanks! _Cheers!_
