![everestheader](https://user-images.githubusercontent.com/23148259/39124644-c886b47a-4719-11e8-953c-f079b3edb664.png)

# Bug reports
> Reproducible bugs are fixable bugs.

- Describe the issue in as much detail as possible.
- Give proper steps which can be used to reproduce the bug.
- Everest comes with a CLI BugReporter. Use it to generate a report which includes the log files, system details (JVM vendor, version, OS) along with your
experience. Upload the generated zip file while opening an issue. The BugReporter can be found in the `Everest/` directory within the
installation directory. Run it using `java -jar BugReporter.jar`.
- Screenshots, animated GIFs or videos illustrating the bug would be very helpful.

# Pull Requests
- Make sure your code adheres to the style guides that follow.
- Describe your changes in as much detail as possible.
- If your PR is a bug fix, first open an issue pertaining to the issue and then work on the PR.
- If your PR is a feature addition, discuss it with me before, especially so if it is UI/UX related. You can open an issue or email me at rohitawate121@gmail.com.
- Avoid platform-dependent code. Everest should at least work on Windows, Mac and the common Linux distributions like Ubuntu, Fedora, etc.
- Screenshots, animated GIFs or videos showing Everest before and after the PR will be appreciated.
- Please do not feel bad if I ask for some changes to the PR or make some myself or if I have to decline it. I tend to nitpick a
lot when it comes to code but that is only to make sure that the code driving the end product is as beautiful and consistent as
the product itself.

# Java Styleguide
- Use a tab width of **4** for indentation. **(no spaces)**
- Class names must be `UpperCamelCase`.
- Methods and other class members must be `lowerCamelCase`.
- Variable names such as `num0` or `num1` are strictly prohibited.
- JavaFX controllers must have the word 'Controller' at the end.
For example, for the 'Dashboard.fxml' file, the controller class should be DashboardController.
- FXML-annotated variables must be grouped together just after the controller class begins.
Leave no blank lines between these. Group variables of the same type together. (Similar to how the StackPanes and VBoxes are grouped in the example)
```java
public class HomeWindowController implements Initializable {
	@FXML 
	private StackPane homeWindowSP;
	@FXML
	private SplitPane splitPane;
	@FXML
	private TabPane tabPane;
	@FXML
	private TextField historyTextField;
	@FXML
	private VBox historyTab, searchBox, historyPane;
	@FXML
	private StackPane historyPromptLayer, searchLayer, searchFailedLayer;
	@FXML
	private JFXButton clearSearchFieldButton;

	private HashMap<Tab, DashboardController> tabControllerMap;
	private List<HistoryItemController> historyItemControllers;
```
- Non-FXML-annotated class members should be grouped together as shown above. Leave a blank line after the FXML-annotated members and begin.
- For `if`, `switch`, `catch` statements and all the loops, add a single space before and after the opening and closing parenthesis:
```java
if (condition) {

}

switch (name) {

}

try {

} catch (SQLException e) {

}

for (int i = 0; i < 15; i++) {

}

while (condition) {

}

do {

} while (condition);
```
- For methods, only add a single space after the closing parenthesis but not the opening one:
```java
String getAddress() {

}
```

### Conditionals
- For if-else statements with **bodies longer than one line**:
```java
if (condition) {
	
} else {
	
}
```
- `switch` cases should be indented with 4 tabs as so:
```java
switch (bodyTabController.rawInputTypeBox.getValue()) {
	case "PLAIN TEXT":
		contentType = MediaType.TEXT_PLAIN;
		break;
	case "JSON":
		contentType = MediaType.APPLICATION_JSON;
		break;
	case "XML":
		contentType = MediaType.APPLICATION_XML;
		break;
	case "HTML":
		contentType = MediaType.TEXT_HTML;
		break;
	default:
		contentType = MediaType.TEXT_PLAIN;
}
```

# CSS Styleguide
- Use a tab width of **4** for indentation. **(no spaces)**
- Separate styles with one blank line. (as shown in the second example)
- If applying the same style to a bunch of selectors, separate them using lines:
```css
#historyScrollPane,
#historyScrollPane .viewport,
#historyScrollPane .scroll-bar:vertical,
#historyScrollPane .scroll-bar:horizontal {
    -fx-background-color: #404040;
}
```

- Group styles for the same UI element together and add a comment with the element's name before the styles.
```css
/* History item */
#historyItemBox {
    -fx-background-color: #353535;
}

#historyItemBox:hover {
    -fx-background-color: #282828;
}

#rawInputArea {
    -fx-font-size: 15px;
}
```

# Git commit messages
- As of writing this guide (June 26, 2018), I've been doing "Added feature" instead of "Add feature" ie. not being imperative with my commit messages.
This is considered bad practice so all commit messages will be imperative now onwards. Please follow the same practice.
- Start with a capital letter. ("Add feature", not "add feature")
- Do not end with a period.
- Keep the commit history clean. Avoid "Fix blah blah" followed by "Fix blah blah #2" or "Fix blah blah #3".
- Reference issues with '#' for fixes. (Fix #15)
