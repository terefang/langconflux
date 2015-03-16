this code is based on concepts established in:

language confluxer (http://generators.christopherpound.com/)
written by Christopher Pound (pound@rice.edu), July 1993.

over the years various improvements have been made to the original functionality,
especially using java instead of the brutish perl language,
your milage may vary.

Conflux will :

  * read one or many sample text file(s)
  * build internal lookup table(s)
  * generate (random) output that looks similar to the sample(s)

you can influence the output given the various options.

the most simple usage would be:

```
java -jar conflux.jar -i <sample-file>
```