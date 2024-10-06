package com.spring.bittlebittle.user.service;


import com.spring.bittlebittle.user.vo.User;
import com.spring.bittlebittle.user.vo.UserJwt;

import java.util.List;

public interface AdminUserService {

	// update
	int updateUser(User user);

	// delete
	int deleteUser(User user);

	// ??ü???

	List<User> selectUsers();

	// ???????

	User selectListbyNo(User user);
	
	//????????
	
	 int adminUser(User user);
	 
	// Boolean registerJwtWithIdx(UserAuthentication userAuthentication);

	    UserJwt getUserJwt(UserJwt userJwt);

	    UserJwt createUserJwt(UserJwt userJwt);

	    UserJwt editUserJwt(UserJwt build);

	    UserJwt getUserJwtBySubject(UserJwt build);

	    int removeUserJwt(UserJwt userJwt);
	    
	    Boolean loginUser(User user);
	    
//	    // selectOne
//	    User getUser(User user);

}
