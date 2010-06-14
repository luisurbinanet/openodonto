package br.ueg.openodonto.persistencia.dao;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;

import br.ueg.openodonto.dominio.Paciente;
import br.ueg.openodonto.dominio.Pessoa;
import br.ueg.openodonto.dominio.Telefone;
import br.ueg.openodonto.persistencia.EntityManager;
import br.ueg.openodonto.persistencia.dao.sql.CrudQuery;
import br.ueg.openodonto.persistencia.dao.sql.IQuery;
import br.ueg.openodonto.persistencia.dao.sql.QueryExecutor;
import br.ueg.openodonto.persistencia.dao.sql.SqlExecutor;
import br.ueg.openodonto.persistencia.orm.OrmFormat;

@SuppressWarnings("serial")
public class DaoCrudPaciente extends BaseDAO<Paciente>{

	private SqlExecutor<Paciente>     sqlExecutor;
	
	static{		
		initQueryMap();
	}
	

	public static void main(String[] args) {
		DaoCrudPaciente dao = new DaoCrudPaciente();
		Paciente paciente = new Paciente();
		paciente.setCodigo(1L);
		OrmFormat orm = new OrmFormat(paciente);
		Map<String , Object> params = orm.formatKey();
		try {
			System.out.println(dao.hasInheritanceConstraint(Pessoa.class, params));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	public static void main(String[] args) throws SQLException {
		DaoCrudPaciente dao = new DaoCrudPaciente();
		dao.listar("codigo");
		ResultSet rs = dao.getConnection().getMetaData().getExportedKeys(null, null, "pessoas");
		int count = rs.getMetaData().getColumnCount();
		for(int i  = 1 ; i <= count;i++){
			System.out.format("%20s", rs.getMetaData().getColumnName(i));
		}
		System.out.println();
		while(rs.next()){
			for(int i  = 1 ; i <= count;i++){
				System.out.format("%20s", rs.getString(i));
			}
			System.out.println();
		}
	}
	*/
	public static void initQueryMap(){
		BaseDAO.getStoredQuerysMap().put("Paciente.BuscaByNome","WHERE ps.nome LIKE ?");
		BaseDAO.getStoredQuerysMap().put("Paciente.BuscaByCodigo","WHERE ps.id = ?");
		BaseDAO.getStoredQuerysMap().put("Paciente.BuscaByCPF","WHERE pc.cpf = ?");
		BaseDAO.getStoredQuerysMap().put("Paciente.BuscaByEmail","WHERE ps.email = ?");
	}

	public DaoCrudPaciente() {
		super(Paciente.class);
		sqlExecutor = new QueryExecutor<Paciente>(this);
	}
	
	@Override
    public Paciente getNewEntity(){
    	return new Paciente();
    }
	
	private void updateRelationshipTelefone(Paciente o) throws Exception{
		if(o.getTelefone() != null){
			EntityManager<Telefone> entityManagerTelefone = DaoFactory.getInstance().getDao(Telefone.class);
			List<Telefone> todos = getTelefonesFromPaciente(o.getCodigo());
			for(Telefone telefone : todos){
				if(!o.getTelefone().contains(telefone)){
					entityManagerTelefone.remover(telefone);
				}
			}
			for(Telefone telefone : o.getTelefone()){
				telefone.setIdPessoa(o.getCodigo());
				entityManagerTelefone.alterar(telefone);
				getConnection().setAutoCommit(false);
			}
		}
	}

	@Override
	public void alterar(Paciente o) throws Exception {
		if(o != null && o.getCodigo() != null &&  pesquisar(o.getCodigo()) != null){
			Savepoint save = null;
			try{
				if(o == null){
					return;
				}
				getConnection().setAutoCommit(false);
				save = getConnection().setSavepoint("Before Update Paciente - Savepoint");
				OrmFormat orm = new OrmFormat(o);
				update(o, orm.formatKey());
				updateRelationshipTelefone(o);
			}catch(Exception ex){
				ex.printStackTrace();
				if(save != null){
					getConnection().rollback(save);
				}
				throw ex;
			}
			getConnection().setAutoCommit(true);
		}else if(o != null){
			inserir(o);
		}
	}

	private List<Telefone> getTelefonesFromPaciente(Long id) throws SQLException{		
		EntityManager<Telefone> emTelefone = DaoFactory.getInstance().getDao(Telefone.class);
		OrmFormat orm = new OrmFormat(new Telefone(id));
		IQuery query = CrudQuery.getSelectQuery(Telefone.class, orm.formatNotNull() , "*");		
		return emTelefone.getSqlExecutor().executarQuery(query);
	}


	@Override
	public void inserir(Paciente o) throws Exception {
		Savepoint save = null;
		try{
			if(o == null){
				return;
			}
			getConnection().setAutoCommit(false);
			save = getConnection().setSavepoint("Before Insert Paciente - Savepoint");
			insert(o);
			updateRelationshipTelefone(o);
		}catch(Exception ex){
			ex.printStackTrace();
			if(save != null){
				getConnection().rollback(save);
			}
			throw ex;
		}
		getConnection().setAutoCommit(true);
	}

	@Override
	public List<Paciente> listar() {
		try{
			List<Paciente> lista = listar();
			for(Paciente paciente : lista){
				paciente.setTelefone(getTelefonesFromPaciente(paciente.getCodigo()));
			}
			return lista;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Paciente pesquisar(Object key) {
		Long id = Long.parseLong(String.valueOf(key));
		OrmFormat orm = new OrmFormat(new Paciente(id));
		IQuery query = CrudQuery.getSelectQuery(Paciente.class, orm.formatNotNull(), "*");
		List<Paciente> lista;
		try {
			lista = getSqlExecutor().executarQuery(query.getQuery(), query.getParams(), 1);
			if(lista.size() == 1){
				return lista.get(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remover(Paciente o) throws Exception {
		Savepoint save = null;
		try{
			getConnection().setAutoCommit(false);
			save = getConnection().setSavepoint("Before Remove Paciente - Savepoint");
			EntityManager<Telefone> entityManagerTelefone = DaoFactory.getInstance().getDao(Telefone.class);
			for(Telefone telefone : o.getTelefone()){
				entityManagerTelefone.remover(telefone);
			}
			OrmFormat orm = new OrmFormat(o);
			Map<String , Object> params = orm.formatKey();
			remove(params, true);
		}catch(Exception ex){
			ex.printStackTrace();
			if(save != null){
				getConnection().rollback(save);
			}
			throw ex;
		}
		getConnection().setAutoCommit(true);
	}

	@Override
	public SqlExecutor<Paciente> getSqlExecutor() {
		return sqlExecutor;
	}
	
	
}