import React,{Component} from 'react';
import ReactDOM from 'react-dom';
import ReactTransition from 'react-addons-css-transition-group';
import $ from 'jquery';

import AnswerUI from './answer.jsx';
import {} from './style.css';

class Thesis extends Component {

	constructor(props) {
		super();
		
		this.handleSearch = this.handleSearch.bind(this);
		this.readInput = this.readInput.bind(this);
		this.state = {
			searchQuery: '',
			answerObject: ''
		};
	}

	handleSearch(e) {
		e.preventDefault();
		
		$.ajax({
			url:this.props.serverApi + this.state.searchQuery,
			dataType: 'json',
			success: function(res) {
				this.setState({
					answerObject:res.answer
				});
			}.bind(this),
			error: function (xhr, status, err) {
				console.log(xhr);
			}.bind(this)
		});
	}

	readInput(e) {
		this.setState({searchQuery:e.target.value});
	}

	render() {
		return(
			<div id="mainContainer">
				<div id="formContainer">
					<h1 className="header">Question Aswering<br/>Data Kabupaten di Nusa Tenggara Barat</h1>
					<form onSubmit={this.handleSearch}>
						<input onChange={this.readInput} placeholder="Masukkan pertanyaan.." type="text" />
						<button onClick={this.handleSearch}>Cari</button>
					</form>
				</div>
				<ReactTransition transitionName="example" transitionEnterTimeout={600} transitionLeaveTimeout={500}>
					<AnswerUI answer={this.state.answerObject} />
				</ReactTransition>
			</div>
		);
	}
}

ReactDOM.render(<Thesis serverApi="http://localhost:8090/web-version/api/qa?q=" />, document.getElementById('container'));