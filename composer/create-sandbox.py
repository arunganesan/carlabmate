#! /usr/bin/env python3.7
import argparse

def main():
    """
    1. Copy over template projects
    2. Symlink and write the settings files
    3. Compile APK, launch React website, and launch linking server
    4. Then, when people sign up, it'll launch the Python scripts per user.
    5. That's it! Honestly not that bad. Doesn't even have to be in a separate place. 
    """


    # link server
    """
    cd SANDBOX/linkserver
    yarn install
    ./bin/rake db:migrate
    """

    # react
    """
    cd SANDBOX/react
    npm install
    npm install --save ../../../library/react/libcarlab
    npm install --save ../../../library/react/user-input
    """

    # android
    """
    cd SANDBOX/android
    echo "include ':app', ':libcarlab', ':android-passthroughs'" > settings.gradle
    echo "rootProject.name='Packaged'" >> settings.gradle
    echo "project(':libcarlab').projectDir = new File(settingsDir, '../../../library/android/libcarlab')" >> settings.gradle
    echo "project(':android-passthroughs').projectDir = new File(settingsDir, '../../../library/android/android-passthroughs')" >> settings.gradle
    """


    """
    cd SANDBOX/android/app
    ff = open('build.gradle', 'r').read()
    ff = ff.replace('/*DEPENDENCIES*/', "implementation project(':android-passthroughs')")
    ofile = open('build.gradle', 'w')
    ofile.write(ff)
    ofile.close()

    cd SANDBOX/android
    # (this might be necessary because we're linking to the same folder)
    ./gradlew build
    APK is saved under: SANDBOX/android/app/build/outputs/apk/debug/
    """

    
    # Python
    """
    ln -sn ../../../library/python/libcarlab
    ln -sn ../../../library/python/obstacle-warning-python obstacle_warning_python
    """


    # start everything 
    """
    ( cd carlabserver && ./bin/rails s -b 0.0.0.0 -p 1234 & )
	( cd sandbox/$(PROJECT)/linkserver && ./bin/rails s -b 0.0.0.0 -p 8080 & )
    """


    # every time you create a new user:
    """
    ( cd sandbox/$(PROJECT)/python && ./packaged.py & )
    """



if __name__ == '__main__':
    import argparse
    
    main()
