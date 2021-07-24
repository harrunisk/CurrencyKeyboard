# CurrencyKeyboard
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
  
  
  
  
<p align="center">
<img src="https://github.com/harrunisk/CurrencyKeyboard/blob/main/art/app.gif" width="300" >
</p>
  
  
  
## Usage  
 ```
    <com.nstudiosappdev.currencykeyboard.CurrencyKeyboard
        android:id="@+id/currencyKeyboard"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:currencyTextSize="14sp"
        app:localeCountry="AE"
        app:localeLanguage="en"
        app:maxCharacterOnIntegerSection="13" />
```
## Setup
1. Add the JitPack repository to your root build.gradle at the end of repositories:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
2. Add the EasySwipe dependency in the build.gradle:
```
implementation 'com.github.harrunisk:CurrencyKeyboard:1.0.0'
```
