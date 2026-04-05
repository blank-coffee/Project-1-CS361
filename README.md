# Project-1-CS361
Group project repo for three-way merge sort and sorting/hashing the password database

For Part 1:
Tester is the runnable file and it only uses MergeSort. The random arrays to be sorted are created dynamically and output is directly printed. 


For Part 2:

Driver and Driver with section prints print out the desired data using a subset of the desired database that's one million passwords. These classes print out the required and most of the mentioned data in the instructions pdf. Bloom filter also has two versions, one used the packed bit array to do the extension part of the project and one using java's bitset. The other three runnable java files, SweepRunner, Histogramexporter, and HashsetMemoryCompare create various csv and txt files used to support our findings here: https://unmm-my.sharepoint.com/:w:/g/personal/jgriego21_unm_edu/IQC-uADLoD6SRILi_FqXTQB6AfnD2iZkgRZifK3xEavLuM8?e=YsIOYr this will also contain instructions to create the csv and txt files (this isn't the pdf we submitted but a document I created to gather our data before writing in scientific format). The data set should be in the zip file we submit but here's instructions to recreate your own data subset:
1. go here: https://github.com/HaveIBeenPwned/PwnedPasswordsDownloader?tab=readme-ov-file#download-all-ntlm-hashes-to-a-single-txt-file-called-pwnedpasswords_ntlmtxt
2. download the 9.0 version: https://dotnet.microsoft.com/en-us/download/dotnet/9.0
3. Open a command line window (I used windows powershell)
4. Run dotnet tool install --global haveibeenpwned-downloader
5. Run haveibeenpwned-downloader.exe pwnedpasswords
6. Run Get-Content pwned-passwords-sha1-ordered-by-hash.txt -TotalCount 1000000 |  Set-Content subset.txt
7. Run (Get-Content subset.txt) -replace ":.*","" | Set-Content hashes.txt
8. Now where ever you ran this, go to the file and move it into your intellij project locally, the .ignore will stop it from pushing
9. I created a folder named passwords and put it in there, then labelled this folder as sources
10. Run any of the part 2 runnable folders after setting their run configurations to where you put the hashes.txt to include it
11. Now it should work for every java file listed here with a main but you'll need to add it to the configurations of each one you want to run.
