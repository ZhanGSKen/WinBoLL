## JC Shell switch to Termux Bash Shell
export HOME=/data/data/com.termux/files/home
export PATH="/data/data/com.termux/files/usr/bin:$PATH"
cd ${HOME}
echo "Home dir is : "$(pwd)
echo "PATH is $PATH"
bash