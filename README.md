
---
Quant-Analysis

---


# 一、 Project Structure
## 1. `assets` Directory
### `query_history`
> To save previous query, with the formula used and the results. 

### `stockHistories`
> To save the K-line data from 2010

### `pref.txt` and `pref-timestamp.txt`
> The current configuration and previous configurations. 

### `rules.syntax`
> The syntax file used by the formula parser. 
> The formula is of a LALR(1) language. 

## 2. `libs` Directory
> Libraries relied.  

## 3. `src` Directory
> The source directory of the project. 

- ssq.stock
>Basic libraries about the updating, storing, and calculating of the stock data

- ssq.stock.analyser
> Provide a traverse framework by defining a base class `ssq.stock.analyser.Analyser` with *evaluate* and *scan* methods to facilitate the procedure of evaluating the grade of the stocks given the query formula and traversing over all the stocks. 
> There are also a few of example classes inheriting from the base class. Among them, there are utilities for accessing various kinds of databases. 
> The core executor of the formula, `ssq.stock.analyser.Interpretor` is also inherited from the base class. 

- ssq.stock.gui
> Provide the essential GUI elements involved in the process of querying stocks, by extending the customed base classes: frame with a status bar, frame with a table in it, frame with a tree in it. 
> Methods for generating K-line graph is also included, but not fully supported for now. 

- ssq.stock.interpreter
> The parser of the formula syntax, and the data structure of the AST (abstract syntax tree). 
> The parser is implemented with the help of [runcc](http://runcc.sourceforge.net/)

- ssq.utils
> [Utilities](https://git.oschina.net/ssqston/util.git) implemented by myself

- ssq.utils.taskdistributer
> The scheduler of muti-thread task

## 4. `SelectStock.jar`
> Exported executable jar file. 
> Before running it, you should have [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or JRE8 installed on your device. 

## 5. `sendkey.exe`
> If you are using the eastmoney client-end software, just open it and double click one of the items listed in the query result. Then you can see the activite window is changed to the eastmoney.exe and the corresponding stock is displayed in it. 
> This exe file is to carry out the steps for you. 

# 二、Usage
> There are 2 standard work flow in this software. 
 - The GUI based query and loopback system, to select the stocks of your desire and test the effecacy of your query formula through the old data. 

![The main window](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/main%20frame.PNG)

![The entrance to loopback](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/loopback%20entrance.PNG)
![The result of loopback](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/loopback%20result.PNG)

![The entrance to debuging](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/debug%20entrance.PNG)
![The result of debuging](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/debug%20detail.PNG)

![The list of query history](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/history%20list.PNG)
![The detail of a single query](https://github.com/ssqstone/Quant-Analysis/blob/master/doc/history%20element.PNG)


 - The programming-oriented way, just making use of the stock data provided by the spider and the evaluating and traversing facilities provided by the `ssq.stock.analyser.Analyser` class. 
Examples can be seen under the `ssq.stock.analyser` package

> Your main efforts should be paid on how to compose your formula
- I have already put an example of the formula in this release. 

```scala
/*All these comments should be removed before you put this formula into the GUI*/
max(250->125).opening/*The opening price*/..norest/*Without adjusting (Backward adjustment is the default option)*/ 
/*The above line is a invoking of a function, to denote 
the maximum of the opening price of the latest 250 day to 125 day, without adjusting the price.*/
* 2 /*The expressions consist with the four arithmetic operations is supported*/
/*Every invoking of a funciton yeilds a value, 
the normal numbers are also values, 
and their results after the four arithmetic operations are still all values.*/
< /*Less than operator*/
average(5->1).highest 
/*There isn't a "..norest". So it yeilds the average of the highest prices in the latest 5 days*/ 
/*All these above make an inequation. 
Every inequation is an item for a stock to be graded. 
Taking the history data of a single stock into the inequation, we will get two values on the two sides. 
The grade of the stock upon this inequation is equal to the "satisfication level" of the inequation, 
which will define it later. */
@2 /*The weighting factor, defauting to 1. 
It is the scale factor of the grades failed. */ 
/*All these above is a composed expressoin. You can build an expression tree by using the 
&&, ||, and ()*/
&& /*And*/
(
3<4 /*Nonsense. Just for demonstration of the gramma. */
|| 
sum(250->1).quantity /*The sum of the deal quantity of the latest 250 days*/ > 10000000000)
/*The priority of &&, ||, and () are just as the same as those in C*/
```

Then you can put this formula into the GUI to get the result. 
```scala
max(250->125).opening..norest * 2 < average(5->1).highest @2 && (3<4 || sum(250->1).quantity > 10000000000)
```
	Note: Just to illustrate the gramma of the formula, use this and all other formula on your own risks. 

> For the experts
The "satisfication level" of an inequation is just a quotient followed by a saturation operation. 

```scala
2 == 3 ======> 2/3			
//just taking the small number as the dividend

3 <= 2 ======> 2/3			
//just taking the right hand side as the dividend

2 <= 3 ======> 3/2 ======> 1		
//the maximum grade is 1

3<=2 && 4==5 || 1 == 3  ======> max(2/3 * 4/5, 1/3) ======> 8/15 
//the composition of the inequations are calculated by multiply and picking the max value
```
    
# 三、About the interfaces
## 1. Java and C
> Can be simply implemented by jni or cmd line

## 2. Java and matlab
> With the help of javabuilder

## 3. Provide data for SPSS
1. Through SQL Server
>By setting the passwd of account *sa* to *00*, or modify the code, you can put all the history data to SQL Server using `ssq.stock.analyser.SqlserverUpdater`
	
	Note: The underlying jdbc should be connected to a certain database. So you should create a database named Stock in the SQL Server Management Studio provided by the SQL Server installation. 

2. By text file
>All the history data can be exported using `ssq.stock.analyser.TextOutPuter`
