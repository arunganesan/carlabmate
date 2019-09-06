#! /usr/bin/env python3

class libcarlab ():
    def __init__ (self):
        '1'
    
    def check_new_info (self):
        return []
    
    def output_new_info (self, name, value):
        # save it to the outbox file
        return True

def main():
    cl = libcarlab()
    
    # check if there is any new info
    cl.check_new_info()

    # take action if needed

    # output info if needed
    cl.output_new_info('mood', 'happy ¬me')

if __name__ == '__main__':
    # test it with dummy data
    main()