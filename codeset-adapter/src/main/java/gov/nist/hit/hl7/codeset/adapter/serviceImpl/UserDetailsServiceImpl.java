package gov.nist.hit.hl7.codeset.adapter.serviceImpl;

import gov.nist.hit.hl7.codeset.adapter.model.ApplicationUser;
import gov.nist.hit.hl7.codeset.adapter.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static java.util.Collections.emptyList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
//
//    public UserDetailsServiceImpl( UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
    public UserDetailsServiceImpl() {

    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        ApplicationUser user = userRepository.findByUsername(username);
//        if (user == null) {
//            throw new UsernameNotFoundException(username);
//        }
//        return new User(user.getUsername(), user.getPassword(), emptyList());
        return  new User(username, username, emptyList() );
    }
}
