# AirZen - An Air Quality Monitor (COEN/ELEC 390 Project)  
A device designed to benefit individuals who work or study at a desk for extended periods, those with respiratory issues like asthma, and people concerned about their surrounding air quality.

## Getting started on the project
### Clone the project
`git clone https://github.com/hulksunil/AirZen.git`  

### Create a branch called <your_name> and checkout to it
`git checkout -b your_name`

### When ready to push your code
1. Add the files to be commited
`git add filename1, filename2,...`

2. Commit them with a resonable message
`git commit -m "a message describing what changes you're making as well as any other important information"`

3. Push your changes to your branch
`git push origin your_name`


## Using the Database in the android app
we need the google-services.json file so it can work.  
Go to the [firebase console](https://console.firebase.google.com/u/0/project/airzen-c1ded/overview?fb_utm_source=studio) under the AirZen project.  
Click on the gear icon at the top left corner and go to project settings.  
Scroll down and download the google-services.json file.  
Place that file in the Android\app folder.  
Now run the project  

# Repository rules
- Everyone will work on their own branch and must do merge requests to staging and all commit must have useful messages. 
- When each milestone is completed, we will merge it to staging to not complicate the main/master branch. 
- Everyone on must communicate with each other to not step on each others toes to avoid making merge conflicts. 
- Whenever doing a pull request, add everyone working on that side as an assignee so at least one person can verify your changes before confirming your merge request.
- If you find bugs, enter it in Github issues and assign it to the team responsible for it
- Do frequent commits. Each commit **does not** mean the feature is done, it just shows everyone that you're actually working

# Coding Standards
- variable names should be relevant
- no redundant code; ie. if it looks like you're using it in many places, make it a function and call it instead.
- remove any unused variables/functions whenever possible to keep code as clean and readable as possible.
- files must be placed under the appropriate folder 

# Basic commands 
These commands can be used in [git bash](https://git-scm.com/downloads) or in any normal terminal with the project in the current working directory
## Preparing to push to your personal branch
- `git status` This will show you all the files edited on your own personal branch. All the red is new changes.
- `git add .` This will prepare all the changes you did. 
- `git status` This will show you everything you have prepared for the commit.
- `git commit -m " useful message"` This will prepare the edits you have done to your own branch with your useful message
- `git push origin [personal branch]` This will update your branch on Github and everyone can see your changes

## Navigation of Branches
- `git checkout -b tester` This will create a branch under the name **tester**
- `git checkout tester2` This will transfer you to the branch under the name **tester2**. You may need to then pull that branch to update it



More updates to come as the project progresses!!
