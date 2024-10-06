package com.spring.bittlebittle.user.service;

import com.spring.bittlebittle.user.dao.AdminUserDao;
import com.spring.bittlebittle.user.vo.User;
import com.spring.bittlebittle.user.vo.UserJwt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserServiceImpl implements AdminUserService {

	private Logger log = LogManager.getLogger("case3");
	
	@Autowired
	private AdminUserDao adminDao;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
//
    @Value("${secretKey}")
    private String key;
	
	
	@Override
	public int updateUser(User user) {
		 return adminDao.updateUser(user);
	}

	@Override
	public int deleteUser(User user) {
		return adminDao.deleteUser(user);
	}

	@Override
	public List<User> selectUsers() {
		 return adminDao.selectUsers();
	}

	@Override
	public User selectListbyNo(User user) {
		return adminDao.selectListbyNo(user);
	}

	@Override //?????
	public int adminUser(User user) {
		 user.setUserPwd(passwordEncoder.encode(user.getUserPwd()));
	        return adminDao.adminRegister(user);
	}
	
	//??? JWT??

	@Override
	public UserJwt getUserJwt(UserJwt userJwt) {
		 return adminDao.selectUserJwt(userJwt);
	}

	@Override
	 @Transactional
	public UserJwt createUserJwt(UserJwt userJwt) {
		 userJwt.setUserJwtIdx(passwordEncoder.encode(userJwt.getSubject() + key));
		 adminDao.insertJwtWithIdx(userJwt);
	        return adminDao.selectUserJwt(userJwt);
	}

	@Override
	 @Transactional
	public UserJwt editUserJwt(UserJwt userJwt) {
		adminDao.updateUserJwt(userJwt);
        return adminDao.selectUserJwt(userJwt);
	}

	@Override
	public UserJwt getUserJwtBySubject(UserJwt userJwt) {
		 return adminDao.selectUserJwtBySubject(userJwt);
	}

	@Override
	public int removeUserJwt(UserJwt userJwt) {
		return adminDao.deleteUserJwt(userJwt);
	}

	@Override
	public Boolean loginUser(User user) {
        User loginUser = adminDao.selectLoginUser(user);

        if (loginUser == null) {
            log.debug("ID None.");
            return false;
        }

//        if (!passwordEncoder.matches(user.getUserPwd(), loginUser.getUserPwd())) {
//            log.debug(passwordEncoder.encode(user.getUserPwd()));
//            log.debug(user.getUserPwd());
//            log.debug(loginUser.getUserPwd());
//            log.debug("PASSWORD FAIL");
//            return false;
//        }

		if (user.getUserPwd() == loginUser.getUserPwd()) {
			log.debug("LOGIN SUCCESS");
			return true;
		} else {
			log.debug(user.getUserPwd());
			log.debug(loginUser.getUserPwd());
			log.debug("PASSWORD FAIL");
			return false;
		}
	}

//	@Override
//	public User getUser(User user) {
//		return adminDao.selectUser(user);
//	}
	
	
	
	

}
