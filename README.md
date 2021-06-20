# Ticks Microservice

The microservice has been developed using Micronaut and Java while the tests have been created using spock and groovy.

# How to run it
If you are using IntelliJ when you import the project, gradle should be automatically recognized and you can use run and
test directly form the plugin.

<img src="https://user-images.githubusercontent.com/59740371/122458612-4db78a80-cfb0-11eb-8ac8-38bf33b65ad7.png" width="500">

Otherwise check if you have JAVA_HOME correclty setup running in the terminal `echo $JAVA_HOME` if there is no response check this 
[handy article from the bible](https://stackoverflow.com/questions/11345193/gradle-does-not-find-tools-jar).

Done with that move to the root folder of the project and execute `./gradlew run`

# How to run the tests

Move to the root folder of the project and execute `./gradlew clean test`

# Assumptions, notes and possible improvements

I put some effort into parallelize as much as i can handling of ticks and into optimising add/remove operations. 
The asymptotically slowest point is the synchronized call to remove ticks from a collection.

I spent sometime thinking to other possible datastructures to use like orded treesets allowing duplicates, queues and similar but i wanted to keep the solution simple.

To store and remove a Tick I used 4 Runnable:
* One to update global stats
* One to update single instrument stats
* two are enqued on tick creation to remove the tick from the data structures and re-calculate the statistics at the right time

I decided that 100% consistency among the single instrument and the global stats endpoints was not always crucial for our system, so i'm not "waiting" for single instrument update to proceed with re-calculation of global stats.
I tried to implement a mitigation of the possible inconcistency on tick removal from the arrays, enqueueing the runnables "earlier" proportionally to the size of the arrays. 
This is a really high level attenuation that would need tuning with an up to speed system, but i wanna make clear that i know that needs more care.

# Coding languages

It's a long time i don't use pure Java, my daily framework is grails+groovy and micronaut+groovy, bear with me if my java is not cutting edge, i don't have a 1:1 mapping among groovy and newer java versions in my fingers :) 

For the tests i used spock because i use it daily.

Requested dependencies have been added into build.gradle hopefully that works out of the box for you!

# Does this scale?

Not really, executuion time increases linearly with number of ticks, a certain degree stability could be reached by observing ticks I/O when the system is up to speed and tune it around the average load value.

I see this task scaled in a event-based distributed system like: multi source -> queue system -> subscribers nodes

# Was it fun?

Yes! Thank you, it was very fun! I did enjoy the challenge, it's well explained and hides some interesting challenges in complexity and parallelization.
