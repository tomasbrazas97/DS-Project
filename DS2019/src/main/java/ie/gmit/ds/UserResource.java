package ie.gmit.ds;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;

@Path("/user")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UserResource {

	private HashMap<Integer, UserResponse> userMapResponse = new HashMap<>();
	private boolean loginTrue;

	PasswordServiceClient client = new PasswordServiceClient("localhost", 50551);

	public UserResource() {

	}

	@GET
	public List<UserResponse> getUsers() {
		return new ArrayList<UserResponse>(userMapResponse.values());
	}

	@POST
	public Response addUser(User user) throws UnsupportedEncodingException {
		
		if(user == null) {
			return Response.status(400).build();
		}
		
		client.makePassword(user.getUserId(), user.getPassword());

		ByteString hashedString = client.getHashedPassword();
		ByteString saltString = client.getSalt();
		
		UserResponse userResponse = new UserResponse(user.getUserId(), user.getUserName(), user.getEmail(), new String(hashedString.toByteArray(), Charset.defaultCharset()), new String(saltString.toByteArray(), Charset.defaultCharset()));
		userMapResponse.put(userResponse.getUserId(), userResponse);
		return Response.status(200).build();
	}

	@Path("/{userId}")
	@GET
	public Response getUserById(@PathParam("userId") int id) {
		if (userMapResponse.get(id) == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		else {
			return Response.ok(userMapResponse.get(id)).build();
		}

	}

	
	@Path("/{userId}")
	@PUT
	public Response changeUser(User user, @PathParam("userId") int id) throws UnsupportedEncodingException {
		if (user == null || userMapResponse.get(id) == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		else {
			userMapResponse.remove(id);
			
			client.makePassword(user.getUserId(), user.getPassword());
			
			ByteString hashedString = client.getHashedPassword();
			ByteString saltString = client.getSalt();
			
			UserResponse userResponse = new UserResponse(user.getUserId(), user.getUserName(), user.getEmail(), new String(hashedString.toByteArray()), new String(saltString.toByteArray()));
			userMapResponse.put(userResponse.getUserId(), userResponse);
			return Response.ok(userResponse).build();
		}

	}

	@Path("/{userId}")
	@DELETE
	public Response deleteUser(@PathParam("userId") int id) {
		if (userMapResponse.get(id) == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		else {
			userMapResponse.remove(id);
			return Response.ok().build();
		}
	}

	
	@Path("/login")
	@POST
	public Response loginUser(Login login) throws UnsupportedEncodingException {
		UserResponse userResponse = userMapResponse.get(login.getUserId());
		
		byte[] pArr = userResponse.getHash().getBytes(Charset.defaultCharset());
		byte[] sArr = userResponse.getSalt().getBytes(Charset.defaultCharset());

		byte[] test = Passwords.hash(login.getPassword().toCharArray(), sArr);
		
		String testString = new String(test, Charset.defaultCharset());
		
		System.out.println(userResponse.getHash());
		System.out.println(testString);
		
		ByteString pBs = ByteString.copyFrom(pArr);
		ByteString sBs = ByteString.copyFrom(sArr);
		
		loginTrue = client.validate(login.getPassword(), pBs, sBs);
		
		String res = "" + loginTrue;
	
		return Response.status(200).entity(res).build();
	}
}
