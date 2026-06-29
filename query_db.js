const { Client } = require('pg');

const client = new Client({
  user: 'postgres',
  host: 'localhost',
  database: 'postgres',
  password: '123',
  port: 5432,
});

async function run() {
  await client.connect();
  try {
    const res = await client.query('SELECT table_name FROM information_schema.tables WHERE table_schema = \'public\'');
    console.log(res.rows.map(r => r.table_name));
    
    // Check if admins or admin exists and query it
    if (res.rows.find(r => r.table_name === 'admin' || r.table_name === 'admins')) {
      const tableName = res.rows.find(r => r.table_name === 'admin') ? 'admin' : 'admins';
      const adminData = await client.query(`SELECT * FROM ${tableName} LIMIT 5`);
      console.log(`\nContents of ${tableName}:`);
      console.log(adminData.rows);
    }
  } catch (err) {
    console.error(err);
  } finally {
    await client.end();
  }
}
run();
