# UpdateChecker
Update Checker For Google Play

@see Kotlin version: [__Kotlinify__](https://github.com/kotlinify/UpdateChecker)

![](/IntroPic/updatechecker.png)

[![](https://jitpack.io/v/kobeumut/UpdateChecker.svg)](https://jitpack.io/#kobeumut/UpdateChecker)

# New Features
* Add new super looking alert dialog
* Fix bugs and improve performance performance fixes
* Auto show Whats New list on Google Play Market by language

**Used Libraries:**

* Jsoup for scrap.
* AwesomeDialog for AlertDialog

**Screenshots:**

![](/IntroPic/updateChecker.gif)

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}


Add the dependency

	dependencies {
	        compile 'com.github.kobeumut:UpdateChecker:0.2.1'
	}
  
  

Or Maven


	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>

 Add the dependency

	<dependency>
	    <groupId>com.github.kobeumut</groupId>
	    <artifactId>UpdateChecker</artifactId>
	    <version>0.2.1</version>
	</dependency>


# Usage


```
 new GoogleChecker(activity, false);
```

Additional parameters is lang for language and package name for search in market

**Usage 1: Package Name**

İf you want to option a cancel you can send second parameters to true.

And if you send a difference package name
```
 new GoogleChecker("com.grisoft.umut.uBackup",activity, false);
```

**Usage 2: Package Name + Language**

```
 new GoogleChecker("com.grisoft.umut.uBackup",activity, false, "en");
```

**Usage 3: Language**

```
 new GoogleChecker(activity, false, "en");
```



# Licence
Copyright 2017 Gri Software Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
