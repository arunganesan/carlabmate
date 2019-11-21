# update and install all required packages (no sudo required as root)
# https://gist.github.com/isaacs/579814#file-only-git-all-the-way-sh


#apt-get update -yq && apt-get upgrade -yq && \
#apt-get install -yq curl git gcc

#curl -sL https://deb.nodesource.com/setup_12.x | bash -
#apt-get install -y nodejs

# fix npm - not the latest version installed by apt-get
#npm install -g npm
#which node; node -v; which npm; npm -v; \
#npm ls -g --depth=0







apt-get -yq install curl
curl -sL https://deb.nodesource.com/setup_12.x | bash -
curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list

apt-get update -yq
apt-get install -yq git-core zlib1g-dev build-essential libssl-dev libreadline-dev libyaml-dev libsqlite3-dev sqlite3 libxml2-dev libxslt1-dev libcurl4-openssl-dev software-properties-common libffi-dev nodejs yarn

git clone https://github.com/rbenv/rbenv.git ~/.rbenv
echo 'export PATH="$HOME/.rbenv/bin:$PATH"' >> ~/.bashrc
echo 'eval "$(rbenv init -)"' >> ~/.bashrc
. ~/.bashrc

git clone https://github.com/rbenv/ruby-build.git ~/.rbenv/plugins/ruby-build
echo 'export PATH="$HOME/.rbenv/plugins/ruby-build/bin:$PATH"' >> ~/.bashrc
. ~/.bashrc

rbenv install 2.6.3
rbenv global 2.6.3
ruby -v

gem install bundler
gem install rails -v 6.0.0
rbenv rehash
rails -v

