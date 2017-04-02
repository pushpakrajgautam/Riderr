# ![alt tag](https://s11.postimg.org/7aez59fir/Untitled.png)

## Inspiration
There is no app that gives us the statistics to optimize our travel. We hope to fix that, using some machine learning. That's why we built Riderr! Riderr helps you give the cheapest prices to travel across the city just by a few taps on the screen. With Riderr, you can get the different time during the day when the travelling prices drop to the lowest. Using machine learning, we predict based on past data the best time to book your Uber for the cheapest travel, saving you over $6 in some cases on just a 10mile ride for example! (LINK: https://github.com/darthcodus/hermes-server)

## What it does
Riderr optimizes your travel by giving you the accurate transit data. In the app, the user can choose to track Uber prices and travel times between any sets of destinations. We planned to use machine learning to optimize travel, which takes into account things like the weather, finding times when prices and travel times would be lowest. That part is still incomplete. 

## How we built it
We used Google Location Services API to access the users location. A server aggregates the prices of travelling between user chosen sources and endpoints. We create a graph to see the price comparisons, and give the optimal prices during the day.

#
![alt tag](https://s23.postimg.org/ct0vlw4bf/Untitled1.png)
#
![alt tag](https://s15.postimg.org/ny393sgnv/Screenshot_2017-04-02_07.01.15.png)

## Challenges we ran into
Fixing over 10 crashes in the app and countably infinite memory leaks to make the app stable for proper daily use.

## Accomplishments that we're proud of
After extensive hours of bugs fixing, Riderr is in stable mode with no more crashes, and is ready for daily use.

## What's next for Riderr
Actually build a machine learning model for the price prediction, we are currently hindered by the lack of enough data.
