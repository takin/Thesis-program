import React,{Component} from 'react';
import ReactDOM from 'react-dom';
import AnswerUI from './answer.jsx';

class Thesis extends Component {

	constructor(props) {
		super();
		
		this.handleSearch = this.handleSearch.bind(this);
		this.readInput = this.readInput.bind(this);
		this.state = {
			searchQuery: '',
			answerText: ''
		};
	}

	handleSearch(e) {
		e.preventDefault();
		fetch(this.props.serverApi)
		.then(res => {
			return res.json();
		})
		.then(res => {
			console.log(res);
			if ( res.code === 200 ){
				this.setState({
					answerText:res.answer,
					searchQuery: ''
				});
			}
		});
	}

	readInput(e) {
		this.setState({searchQuery:e.target.value});
	}

	render() {
		return(
			<h1 className="header">{this.props.title}</h1>
			<div id="formContainer">
				<form onSubmit={this.handleSearch}>
					<input onChange={this.readInput} placeholder="Masukkan pertanyaan.." type="text" />
					<input type="submit" onClick={this.handleSearch} value="Cari" />
				</form>
			</div>
			<AnswerUI answer={this.state.answerText} />
		);
	}
}

Thesis.defaultProps = {
	title: "Sistem Question Aswering Data Kabupaten di Nusa Tenggara Barat",
	serverApi: "/data.json"
};

var node = document.getElementById('container');
ReactDOM.render(<Thesis />, node);