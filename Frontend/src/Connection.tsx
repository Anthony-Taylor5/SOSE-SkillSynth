import { useEffect, useState } from "react";
import { getUsers, createUser, updateUser, deleteUser } from "./components/api";

interface User {
  id: number;
  username: string;
  level: number;
}

function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [newUsername, setNewUsername] = useState("");
  const [newLevel, setNewLevel] = useState(1);

  const fetchUsers = () => {
    getUsers()
      .then(res => setUsers(res.data))
      .catch(err => console.error(err));
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreate = () => {
    createUser({ username: newUsername, level: newLevel })
      .then(() => {
        fetchUsers();
        setNewUsername("");
        setNewLevel(1);
      });
  };

  const handleUpdate = (user: User) => {
    updateUser({ ...user, level: user.level + 1 })
      .then(() => fetchUsers());
  };

  const handleDelete = (id: number) => {
    deleteUser(id).then(() => fetchUsers());
  };

  return (
    <div>
      <h1>All Users</h1>

      <div>
        <input
          type="text"
          placeholder="Username"
          value={newUsername}
          onChange={e => setNewUsername(e.target.value)}
        />
        <input
          type="number"
          placeholder="Level"
          value={newLevel}
          onChange={e => setNewLevel(Number(e.target.value))}
        />
        <button onClick={handleCreate}>Add User</button>
      </div>

      <ul>
        {users.map(u => (
          <li key={u.id}>
            {u.username} — Level {u.level}
            <button onClick={() => handleUpdate(u)}>Level Up</button>
            <button onClick={() => handleDelete(u.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default UsersPage;
