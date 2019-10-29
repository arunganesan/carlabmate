## Notes

* The Activity Java file in your module has to use the `R` file of the CarLabUI layout. Android Studio tends to automatically import the R file of the current module, so make sure that is replaced by `import edu.umich.carlabui.R`. 

* * But, a caveat to this is when we read the user-specific settings in the `values.xml` file. To make sure we read from the right place, make sure to refer it by the full R package name, e.g.: `edu.umich.savad_manual.R.string.uid`

