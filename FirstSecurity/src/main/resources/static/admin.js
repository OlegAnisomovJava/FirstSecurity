document.addEventListener("DOMContentLoaded", function () {
    loadUsers();

    document.getElementById("addUserForm").addEventListener("submit", function (event) {
        event.preventDefault();
        addUser();
    });

    document.getElementById("editUserForm").addEventListener("submit", function (event) {
        event.preventDefault();
        updateUser();
    });
});

function loadUsers() {
    fetch("/api/users")
        .then(response => response.json())
        .then(users => {
            const usersTableBody = document.getElementById("usersTableBody");
            usersTableBody.innerHTML = "";

            users.forEach(user => {
                let roles = user.roles.map(role => role.name).join(", ");
                let row = `
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.firstName}</td>
                        <td>${user.lastName}</td>
                        <td>${user.age}</td>
                        <td>${user.email}</td>
                        <td>${roles}</td>
                        <td>
                            <button class="btn btn-warning btn-sm" onclick="editUser(${user.id})">Edit</button>
                            <button class="btn btn-danger btn-sm" onclick="confirmDeleteUser(${user.id})">Delete</button>
                        </td>
                    </tr>
                `;
                usersTableBody.innerHTML += row;
            });
        })
        .catch(error => console.error("Ошибка загрузки пользователей:", error));
}

function addUser() {
    const roles = Array.from(document.getElementById("addUserRoles").selectedOptions)
        .map(option => ({ name: option.value }));

    const userData = {
        firstName: document.getElementById("addFirstName").value,
        lastName: document.getElementById("addLastName").value,
        age: parseInt(document.getElementById("addAge").value, 10),
        email: document.getElementById("addEmail").value,
        password: document.getElementById("addPassword").value,
        roles: roles
    };

    fetch("/api/users", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData)
    })
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при создании пользователя");
            return response.text();
        })
        .then(() => {
            let modalElement = document.getElementById("addUserModal");
            let modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) modalInstance.hide();
            document.getElementById("addUserForm").reset();
            loadUsers();
        })
        .catch(error => console.error("Ошибка при создании пользователя:", error));
}

function editUser(userId) {
    fetch(`/api/users/${userId}`)
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при загрузке пользователя");
            return response.json();
        })
        .then(user => {
            document.getElementById("editUserId").value = user.id;
            document.getElementById("editFirstName").value = user.firstName;
            document.getElementById("editLastName").value = user.lastName;
            document.getElementById("editAge").value = user.age;
            document.getElementById("editEmail").value = user.email;

            const rolesSelect = document.getElementById("editRoles");
            Array.from(rolesSelect.options).forEach(option => {
                option.selected = user.roles.some(role => role.id == option.getAttribute("data-id"));
            });

            let modal = new bootstrap.Modal(document.getElementById("editUserModal"));
            modal.show();
        })
        .catch(error => console.error("Ошибка загрузки пользователя:", error));
}

function updateUser() {
    const userId = document.getElementById("editUserId").value;
    const updatedUser = {
        id: userId,
        firstName: document.getElementById("editFirstName").value,
        lastName: document.getElementById("editLastName").value,
        age: document.getElementById("editAge").value,
        email: document.getElementById("editEmail").value,
        password: document.getElementById("editPassword").value.trim() || null,
        roles: Array.from(document.getElementById("editRoles").selectedOptions).map(option => ({
            id: parseInt(option.getAttribute("data-id")),
            name: option.value
        }))
    };

    fetch(`/api/users/${userId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(updatedUser)
    })
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при обновлении пользователя");
            return response.text();
        })
        .then(() => {
            loadUsers();
            let modalElement = document.getElementById("editUserModal");
            let modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) modalInstance.hide();
        })
        .catch(error => console.error("Ошибка обновления пользователя:", error));
}

function confirmDeleteUser(userId) {
    fetch(`/api/users/${userId}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById("deleteUserId").value = user.id;
            document.getElementById("deleteFirstName").value = user.firstName;
            document.getElementById("deleteLastName").value = user.lastName;
            document.getElementById("deleteAge").value = user.age;
            document.getElementById("deleteEmail").value = user.email;

            const rolesSelect = document.getElementById("deleteRoles");
            Array.from(rolesSelect.options).forEach(option => {
                option.selected = user.roles.some(role => role.id == option.getAttribute("data-id"));
            });

            let modal = new bootstrap.Modal(document.getElementById("deleteUserModal"));
            modal.show();
        })
        .catch(error => console.error("Ошибка загрузки данных пользователя для удаления:", error));
}

function deleteUser() {
    const userId = document.getElementById("deleteUserId").value;

    fetch(`/api/users/${userId}`, {
        method: "DELETE"
    })
        .then(response => {
            if (!response.ok) throw new Error("Ошибка при удалении пользователя");
            return response.text();
        })
        .then(() => {
            loadUsers();
            let modalElement = document.getElementById("deleteUserModal");
            let modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) modalInstance.hide();
        })
        .catch(error => console.error("Ошибка удаления пользователя:", error));
}
