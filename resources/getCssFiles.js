var links = document.getElementsByTagName("link");
var host = window.location.host;
files = Array();
for (i=0; i<links.length; i++) {
	var link = links[i];
	var rel = link.getAttribute("rel");
	if (rel == "stylesheet") {
		files.push(link.getAttribute("href"));
	}
}
return files;